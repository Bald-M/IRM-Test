# Egg.js to Spring Boot migration runbook

The legacy backend was a clean nested Git checkout at commit `7bde87e` from
`https://github.com/Bald-M/IRM-egg-server.git`. Its source was used only to
recover the observable contract; it is not part of the new runtime.

## Before any cutover

1. Rotate the database, Gmail application and JWT credentials that existed in
   the legacy repository history. Removing defaults from the new code does not
   revoke an exposed credential.
2. Take a database backup and an exact schema-only dump from the production
   MySQL instance. The old Sequelize models did not contain migrations,
   relationships or reliable index metadata, so they are not a complete schema
   authority.
3. Restore that backup into a non-production MySQL instance and run the Java API
   against it before changing production traffic.
4. Check for data that conflicts with the new invariants:

   ```sql
   SELECT LOWER(email), COUNT(*)
   FROM application_user
   GROUP BY LOWER(email)
   HAVING COUNT(*) > 1;

   SELECT app_user_id, COUNT(*)
   FROM student
   GROUP BY app_user_id
   HAVING COUNT(*) > 1;

   SELECT token, COUNT(*)
   FROM authentication_token
   GROUP BY token
   HAVING COUNT(*) > 1;
   ```

   Resolve duplicates deliberately; do not delete rows automatically.

## Compatibility checks

- Existing clear-text passwords remain usable once. The first successful login
  replaces the value with BCrypt; legacy values over BCrypt's input limit use
  the marked SHA-256-plus-BCrypt format. New and reset passwords are always
  hashed the same way.
- Database role `Client` is exposed as `Industry`; both forms are accepted when
  authorizing an existing account.
- Confirm that account status values are `Pending`, `Active`, `Blocked` or
  `Removed`. The old JavaScript code compared these string values to numbers and
  therefore did not reliably enforce them.
- Confirm that JSON stored in `student.internship_options` and
  `student.preferred_companies` is valid before opening an existing application
  in the Vue editor.

## Verification sequence

Run the new API against the restored database and verify, in order:

1. health endpoint and Flyway startup;
2. legacy-user login and automatic BCrypt upgrade;
3. registration, OTP verification and login for both Student and Industry;
4. password-reset OTP, password change and rejection of the previous JWT;
5. student application create/update and self-read;
6. rejection when a student requests another student's record;
7. Admin/Industry list and detail reads;
8. the Vue application through the same-origin Nginx `/api` proxy.

Keep the old API stopped but available for rollback until these checks and a
backup restore drill pass. Never run both implementations as writers against the
same production database during the migration window.
