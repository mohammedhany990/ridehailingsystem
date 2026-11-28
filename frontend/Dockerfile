FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
ARG GOOGLE_CLIENT_ID=
RUN sed -i "s|__GOOGLE_CLIENT_ID__|${GOOGLE_CLIENT_ID}|" src/environments/environment.ts
RUN npx ng build --configuration production

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
EXPOSE 80
