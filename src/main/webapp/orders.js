var orders = {};
var cid = 0;
var wsocket = new WebSocket("ws://localhost:8080/Exchange/order");

var notifStyle =
    `
    border: 2px solid #27af1d;
    display: block;
    opacity: 1;
    `;
var errorStyle =
    `
    border: 2px solid #af1d1d;
    display: block;
    opacity: 1;
    `;

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
        else if (data.status === "FatalFail") {
            fatalError(data);
        }
        else {
            showError(data.status);
        }
    }
}

// wsocket.onopen = function (evt) {
//     for (let order in Object.values(orders)) {
//         addOrder(order);
//     }
// }

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

function sendOrder() {
    event.preventDefault();
    let json = {};
    json.type = "Order";
    json.user = document.getElementById("user").value.trim();
    json.instrument = document.getElementById("instrument").value.trim();
    json.side = document.getElementById("side").value;
    json.price = document.getElementById("price").value.trim();
    json.qty = document.getElementById("qty").value.trim();
    json.clientId = cid++;
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
    }
    else {
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
    document.getElementById("notif").style.cssText = notifStyle;
}

function removeNotif(id) {
    document.getElementById("notiftext").innerText = `Your listing removed, ID: ${id}`;
    document.getElementById("notif").style.cssText = notifStyle;
}

function tradeNotif(id) {
    document.getElementById("notiftext").innerText = `Your order ${id} has been traded!`;
    document.getElementById("notif").style.cssText = notifStyle;
}

function cancelError(order) {
    document.getElementById("notiftext").innerText = `There was a problem canceling your order: ${order.clientId}`;
    document.getElementById("notif").style.cssText = errorStyle;
}

function orderError(order) {
    document.getElementById("notiftext").innerText = `There was a problem sending your order!`;
    document.getElementById("notif").style.cssText = errorStyle;
}

function fatalError(order) {
    wsocket.close();
    document.getElementById("notiftext").innerText = `Error occurred, closing connection!`;
    document.getElementById("notif").style.cssText = errorStyle;
}

function showError(error) {
    document.getElementById("invalid-data").innerText = "Please enter valid: " + error;
    document.getElementById("invalid-data").style.display = "block";
}

function fadeElem(id) {
    var tmp = document.getElementById(id).style;
    (function fade(){ (tmp.opacity -= 0.1) < 0 ? tmp.display = "none" : setTimeout(fade,40)})();
}