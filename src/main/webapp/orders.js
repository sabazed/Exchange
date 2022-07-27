var orders = {};
var order_id = 0;
var request_id = 0;
var wsocket = new WebSocket("ws://localhost:8080/Exchange/order");

wsocket.onmessage = function (evt) {
    console.log(evt.data);
    let data = JSON.parse(evt.data);
    if (data.type === "List") {
        addOrder(data);
    }
    else if (data.type === "Trade") {
        tradeOrder(data);
    }
    else if (data.type === "Remove") {
        removeOrder(data.clientId);
    }
    else if (data.type === "Fail") {
        if (data.status === "OrderFail") {
            orderError(data);
        }
        else if (data.status === "CancelFail") {
            cancelError(data);
        }
        else if (data.status === "RequestFail") {
            requestError();
            wsocket.close();
        }
        else {
            showError(data.status);
        }
    }
    else if (data.type === "Response") {
        loadInstruments(data.instruments);
    }
}

wsocket.onopen = function (evt) {
    let json = {};
    json.type = "Request";
    json.clientId = request_id++;
    console.log(json);
    wsocket.send(JSON.stringify(json));
}

// function login() {
//     let user = document.getElementById("username").value;
//     if (orders.hasOwnProperty(user.toString())) {
//         loadOrders(user);
//     }
//     else {
//         orders[user] = {};
//     }
//     document.getElementById("username").value = user;
// }
//
// function loadOrders(user) {
//     for (let order in orders[user]) {
//         addOrder(order);
//     }
// }

function loadInstruments(instruments) {
    for (let instrument of instruments) {
        document.getElementById("instrument").innerHTML += `<option value=${instrument.id}>${instrument.name}</option>`;
    }
}

function sendOrder() {
    event.preventDefault();
    let json = {};
    json.type = "Order";
    json.user = document.getElementById("user").value.trim();
    json.side = document.getElementById("side").value;
    json.price = document.getElementById("price").value.trim();
    json.qty = document.getElementById("qty").value.trim();
    json.clientId = order_id++;
    let menu = document.getElementById("instrument");
    json.instrument = {};
    json.instrument.id = menu.value;
    json.instrument.name = menu.options[menu.selectedIndex].text;
    console.log(json);
    wsocket.send(JSON.stringify(json));
    document.getElementById("invalid-data").innerText = "";
    document.getElementById("invalid-data").style.display = "none";
}

function sendRemove(bid) {
    event.preventDefault();
    let arr = bid.split('-');
    let json = {};
    json.type = "Cancel";
    json.instrument = orders[arr[1]].instrument;
    json.side = orders[arr[1]].side;
    json.clientId = orders[arr[1]].clientId;
    json.globalId = orders[arr[1]].globalId;
    console.log(json);
    wsocket.send(JSON.stringify(json));
}

function addOrder(order) {
    event.preventDefault();
    orders[order.clientId] = order;
    let color = order.side === "BUY" ? "green" : "red";
    let newdiv =
        `                <div class=\"entry\" id="${order.clientId}">\n` +
        `                    <button type=\"submit\" class=\"cancel\" id=\"${order.user}-${order.clientId}" onclick="sendRemove(this.id)">x</button>\n` +
        `                    <span style=\"color: ${color}\">${order.side} Order </span><span>${order.clientId} - Instrument: ${order.instrument.name}, User: ${order.user}<br>Price: ${order.price}$, Quantity: ${order.qty}</span>\n` +
        "                </div>";
    document.getElementById("book-field").innerHTML += newdiv;
    listNotif(order.clientId);
}

function tradeOrder(trade) {
    event.preventDefault();
    let order = orders[trade.clientId];
    order.qty = trade.qty;
    if (order.qty > 0) {
        let color = order.side === "BUY" ? "green" : "red";
        document.getElementById(order.clientId).innerHTML =
            `                    <button type=\"submit\" class=\"cancel\" id=\"${order.user}-${order.clientId}" onclick="sendRemove(this.id)">x</button>\n` +
            `                    <span style=\"color: ${color}\">${order.side} Order </span><span>${order.clientId} - Instrument: ${order.instrument.name}, User: ${order.user}<br> Price: ${order.price}$, Quantity: ${order.qty}</span>\n`;
    } else {
        removeOrder(order.clientId);
    }
    tradeNotif(order.clientId);
}

function removeOrder(bid) {
    event.preventDefault();
    document.getElementById(bid).remove();
    delete orders[bid];
    removeNotif(bid);
}


function listNotif(id) {
    document.getElementById("notiftext").innerText = `New order listed, entry ID: ${id}`;
    document.getElementById("overlay").className = "notif";
    document.getElementById("overlay").classList.remove("invisible");
}

function removeNotif(id) {
    document.getElementById("notiftext").innerText = `Your listing removed, ID: ${id}`;
    document.getElementById("overlay").className = "notif";
    document.getElementById("overlay").classList.remove("invisible");
}

function tradeNotif(id) {
    document.getElementById("notiftext").innerText = `Your order ${id} has been traded!`;
    document.getElementById("overlay").className = "notif";
    document.getElementById("overlay").classList.remove("invisible");
}

function cancelError(order) {
    document.getElementById("notiftext").innerText = `There was a problem canceling your order: ${order.clientId}`;
    document.getElementById("overlay").className = "error";
    document.getElementById("overlay").classList.remove("invisible");
}

function orderError() {
    document.getElementById("notiftext").innerText = `There was a problem sending your order!`;
    document.getElementById("overlay").className = "error";
    document.getElementById("overlay").classList.remove("invisible");
}

function requestError() {
    document.getElementById("notiftext").innerText = `There was a problem connecting to the server disconnecting!`;
    document.getElementById("overlay").className = "error";
    document.getElementById("overlay").classList.remove("invisible");
}

function showError(error) {
    document.getElementById("invalid-data").innerText = "Please enter valid: " + error;
    document.getElementById("invalid-data").style.display = "block";
}

function fadeElem(id) {
    document.getElementById(id).classList.add("invisible");
}