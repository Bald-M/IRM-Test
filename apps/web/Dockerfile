FROM node:22.22.2-bookworm-slim AS build

WORKDIR /workspace

COPY package*.json ./
COPY nx.json ./
COPY apps/web ./apps/web
RUN npm ci
RUN npx nx build web

FROM nginx:1.28.0-alpine

COPY --from=build /workspace/dist/apps/web /usr/share/nginx/html
COPY apps/web/default.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
