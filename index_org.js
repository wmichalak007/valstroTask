/** @format */
const express = require("express");
const { createServer } = require("http");
const { Server } = require("socket.io");
const { doSearch } = require('./swapi-api')


/**
 * Topic for incoming search queries
 */
const TOPIC_SEARCH = "search";


// Constants
const PORT = 3000;
const HOST = "0.0.0.0";
const app = express();
const httpServer = createServer(app);
const server = new Server(httpServer);

server.on("connection", (socket) => {
  console.log("got connection");

  socket.on(TOPIC_SEARCH, async (message) => {
    const matches = await doSearch(message.query);
    const timer = ms => new Promise(res => setTimeout(res, ms))
    // console.log(JSON.stringify(matches, null, 2));
    for(let i = 0; i<matches.length; i++){
      if(matches[i].delay){
        // console.log(JSON.stringify(matches[i], null, 2));
        const {delay, ...resultWithoutDelay} = {...matches[i]};
        socket.emit(TOPIC_SEARCH, resultWithoutDelay);
        // await timer(matches[i].delay || 0);
        await timer(delay || 0);
      }
      else{
        socket.emit(TOPIC_SEARCH, matches[i]);
      }
    }
  });



  socket.on("disconnect", () => {
    console.log(`lost connection with ${socket.id}`);

    // console.log(server.sockets.sockets.values());
  });

  console.log(`connection established with ${socket.id}`);
});


console.log(`Running on http://${HOST}:${PORT} (env: ${process.env.NODE_ENV})`);
httpServer.listen(PORT, HOST);
