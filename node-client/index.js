#!/usr/bin/env node

const WebSocket = require("ws");
const readline = require("readline");

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

let args = process.argv;
let defaultWsUrl;
if (args && args.length === 3) {
    defaultWsUrl =  "ws://localhost:" + args[2] + "/ws"
} else {
    defaultWsUrl =  "ws://localhost:" + 10010 + "/ws"
}
console.log(defaultWsUrl);

function createWs(url) {
    let w;
    try {
        w = new WebSocket(url);
    } catch (e) {
        console.log(e);
        w = {on: (a, b) => a, close: () => null};
    }
    w.on("open", function open() {
        console.log("connected: " + w.url);
        rl.prompt();
    });

    w.on("message", function incoming(data) {
        console.log(data);
        rl.prompt();
    });
    return w;
}

let ws = createWs(defaultWsUrl);

rl.on("line", input => {
    let oChild = console;
    if (input.startsWith("connect ")) {
        let splitStr = input.split(" ");
        if (splitStr.length !== 2) {
            oChild.log("[client] connect command not match! Should be 'connect youWsUrl'");
        } else {
            let wsUrl = splitStr[1];
            ws.close();
            ws = createWs(wsUrl);
        }
        rl.prompt();
        return;
    }

    if (input.equals("help")) {
        oChild.log('[client] jugg is a Java REPL, you can eval your Java Code but don\'t be EVIL!');
        oChild.log(" ");
        oChild.log("connect youWsUrl                         ==== connect another WebSocketServer");
        oChild.log("login username password                  ==== login with your userName and password");
        oChild.log("bean.method()                            ==== call bean method without param");
        oChild.log("bean.m(xxx)                              ==== call bean method with param xxx (auto detect type)");
        oChild.log("bean.m(123L)                             ==== call bean method with param 123 (long type)");
        rl.prompt();
        return;
    }
    ws.send(input);
});
