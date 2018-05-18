FROM node:6.2.0

# Create app directory
RUN mkdir -p /usr/src/bigital-bakcend
WORKDIR /usr/src/bigital-bakcend

# Bundle app source
COPY . /usr/src/bigital-bakcend

# Install app dependencies
# CMD ["npm", "install"]

ENV port 8080

EXPOSE 8080

CMD rm -rf node_modules && npm install && npm run babel && npm run start