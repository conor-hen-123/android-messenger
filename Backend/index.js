//To use the express module
const express = require('express');
//To send HTTP requests
http = require('http');
//Initializing express
app = express();
//Creating server
server = http.createServer(app);

//Needed for database
var MongoClient = require('mongodb').MongoClient;
var url = 'mongodb://localhost:27017/data';
//var chatroom;
var keycounter = 0;
var roomcounter = 0;

//Initialize instance of Socket.io
io = require('socket.io').listen(server);

MongoClient.connect(url, function(err, data){
  if(err){
    console.log('Unable to connect to the server', err);
  } else{
    console.log("Connected to Database");
  }
});

const bcrypt = require('bcrypt');
const saltRounds = 10;

//generating salt and hash, storing username and
//hashed password pair in DB
function createUser(userName, plaintextPassword){
  bcrypt.genSalt(saltRounds,function(err, salt) {
    bcrypt.hash(plaintextPassword, salt, function(err, hash) {
            db.users.insert({'username':userName});
            userName.insert({'password':hash});
    });
  });
}


app.get('/', (req, res) => {
  res.send('Chat Server: port 3000');
});

io.on('connection', (socket) => {

  console.log('user connected');

  socket.on('join', function(userUsername) {

    function confirmUser(userName, plaintextPassword){

      if(bcrypt.compareSync(myPlaintextPassword, db.users.find({"username":userName}).password) == true){
          console.log(userUsername +" : has logged in"  );
      }
      else
        console.log("log in failed");

  }
}); 

    //socket.broadcast.emit('userjoinedthechat',userUsername +" : has joined the chat ");

  
  socket.on('connectroom', function(room) {
    socket.join(room);
    console.log("conected to", room);
    roomcounter = roomcounter+1;
    if (roomcounter == 2){
      console.log("both parties connected");
      socket.broadcast.emit('sendkeyalarm');
    }
  });

  socket.on('sendpublickey', function(publickey, ownroom, friendroom) {
    //socket.send(JSON.stringify(publickey));
    console.log("public key:", publickey);
    keycounter = keycounter+1;
    if(keycounter==1){
      console.log("sending public key from ", ownroom, " to ", friendroom);
      io.sockets.in(friendroom).emit('receivepublickey', publickey);
      io.sockets.in(friendroom).emit('test');
      io.to(ownroom).emit('generatesharedkey');
    }  
  });



  socket.on('userjoinedchat', function(userUsername){    
    console.log(userUsername +" : is watching the chat"  );
  });
    db.createCollection('test', function(err, users) {});
    db.users.insert({'username':'userNickname'});
    users.find();

  socket.on('messagedetection', (senderUsername,cipherText) => {
       
       // Debugging: show user and message in console
       console.log(senderUsername+" :" +cipherText);
        
       //create message object

      let  message = {"ciphertext":cipherText,
                      "senderUsername":senderUsername};
          
    // Send the message to user including the sender using io.emit  

      io.emit('message', message );
    

      });

  socket.on('sendimage', function(data){
    console.log('user sending image' );
    io.emit('receiveimage', data);
    console.log('user receiving image' );
});

		
  socket.on('disconnect', function(userUsername) {
    console.log( userUsername +'user has left ');
    //socket.in(chatroom).emit( "userdisconnect" ,"user has left");

});

 });

server.listen(3000,()=>{

console.log('Node app is running on port 3000');

});
