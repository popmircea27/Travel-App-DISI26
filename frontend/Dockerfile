# Etapa 1: Build (Vite)
FROM node:20-alpine AS builder
WORKDIR /app

# Copiază fișierele de dependențe
COPY package*.json ./

# Instalează dependențele (folosește npm ci pentru reproducibilitate)
RUN npm ci

# Copiază restul codului sursă
COPY . .

# Build aplicației (prod)
RUN npm run build

# Etapa 2: Runtime (Nginx)
FROM nginx:1.27-alpine

# Copiază configurația personalizată Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copiază fișierele build (din etapa 1) în directorul servit de Nginx
COPY --from=builder /app/dist /usr/share/nginx/html

# Expune portul 80
EXPOSE 80

# Pornește Nginx
CMD ["nginx", "-g", "daemon off;"]