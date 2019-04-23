import Console from 'react-console-component';
import 'react-console-component/main.css';
import './console.scss';

import React, { Component } from 'react';

class App extends Component {

  child = (function () {
    let oChild = { console: null, count: 0 };

    let createFireClient = function (clientOChild, wsUrl) {
      let connected = false;
      let ws;
      try {
        ws = new WebSocket(wsUrl);
      } catch (e) {
        ws = {};
        clientOChild.clientLog("connect failed.");
      }

      ws.onopen = function open() {
        connected = true;
        clientOChild.clientLog("connected.");
      };

      ws.onmessage = function incoming(data) {
        let message = data.data;
        clientOChild.log(message);
      };

      ws.onclose = function outline() {
        if (connected) {
          clientOChild.clientLog("connection closed.");
        } else {
          clientOChild.clientLog("connect failed.");
        }
      };

      ws.trySend = function (message) {
        if (connected) {
          ws.send(message);
        } else {
          clientOChild.clientLog("connection lost.");
        }
      };
      return ws;
    };

    oChild.setCon = function (con) {
      oChild.console = con;
      let location = window.location;
      let localWs = 'ws://' + location.hostname + (location.port ? ':' + location.port : '') + '/ws';
      oChild.console.state.promptText = "connect " + localWs;
      oChild.console.acceptLine();
      oChild.console.return();
    };

    oChild.enterLine = function () {
      if (oChild.console) {
        oChild.count = oChild.count + 1;
        oChild.console.return();
      }
    };

    oChild.handleMessage = function (message) {
      // system control message.
      if (message.startsWith("connect ")) {
        let splitStr = message.split(" ");
        if (splitStr.length !== 2) {
          oChild.clientLog("connect command not match! Should be 'connect WsUrl'");
        } else {
          let wsUrl = splitStr[1];
          oChild.fireClient = createFireClient(oChild, wsUrl);
        }
        // should always enter line.
        oChild.enterLine();
        return;
      }

      if (oChild.fireClient) {
        oChild.fireClient.trySend(message);
      } else {
        oChild.clientLog("connection lost");
      }
      oChild.enterLine();
    };

    oChild.log = function (text) {
      if (oChild.console) {
        oChild.console.log(text);
        oChild.console.scrollToBottom();
      }
    };

    oChild.clientLog = function (text) {
      oChild.log('[client] ' + text);
    };
    return oChild;
  })();

  promptLabel = () => {
    return this.child.count + '> ';
  };

  render() {
    return (
      <div
        style={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          height: '100vh',
        }}
      >
        <Console
          ref={ref => (this.child.setCon(ref))}
          handler={this.child.handleMessage}
          promptLabel={this.promptLabel}
          welcomeMessage={'Welcome jugg, please login.'}
          autofocus={true}
        />
      </div>
    );
  }
}

export default App;
