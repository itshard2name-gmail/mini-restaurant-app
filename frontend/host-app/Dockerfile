FROM node:18-alpine as build-stage
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .

# Build Arguments for Vite
ARG PORT_FRONTEND_MENU
ARG PORT_FRONTEND_ADMIN
ARG PORT_ENVOY

# Environment Variables for Vite Build
ENV PORT_FRONTEND_MENU=$PORT_FRONTEND_MENU
ENV PORT_FRONTEND_ADMIN=$PORT_FRONTEND_ADMIN
ENV PORT_ENVOY=$PORT_ENVOY

RUN npm run build

FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
