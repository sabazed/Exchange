var orders = {};
var errors = [];
var cid = 0;
var wsocket = new WebSocket("ws://localhost:8080/Exchange/order");

wsocket.onmessage = function (evt) {
    console.log(evt.data);
    let data = JSON.parse(evt.data);
    console.log(data.status.length);
    if (data.status.length === 1) {
        if (data.status[0] === "Cancel") {
            removeOrder(data.order.clientId);
        } else if (data.status[0] === "Trade") {
            tradeOrder(data.order);
        } else if (data.status[0] === "List") {
            addOrder(data.order);
        } else if (data.status[0] === "Fail") {
            error();
        } else if (data.status[0] === "CancelFail") {
            cancelError(data.order);
        } else if (data.status[0] === "OrderFail") {
            orderError(data.order);
        } else if (data.status[0] === "ListFail") {
            listError();
        }
    }
    else {
        for (let code of data.status) {
            if (code === "InvalidUser") {
                errors.push("username");
            }
            else if (code === "InvalidInstr") {
                errors.push("instrument");
            }
            else if (code === "InvalidPrice") {
                errors.push("price");
            }
            else if (code === "InvalidQty") {
                errors.push("quantity");
            }
        }
        showErrors();
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
    json.status = "List";
    json.order = {};
    json.order.user = document.getElementById("user").value;
    json.order.instrument = document.getElementById("instrument").value;
    json.order.side = document.getElementById("side").value;
    json.order.price = document.getElementById("price").value;
    json.order.qty = document.getElementById("qty").value;
    json.order.clientId = cid++;
    console.log(json);
    wsocket.send(JSON.stringify(json));
    errors = [];
    document.getElementById("invalid-data").innerText = "";
    document.getElementById("invalid-data").style.display = "none";
}

function sendRemove(bid) {
    event.preventDefault();
    let arr = bid.split('-');
    let json = {};
    json.status = "Cancel";
    json.order = orders[arr[1]];
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
        `                    <span style=\"color: ${color}\">${order.side} Order </span><span>${order.clientId} - Instrument ID: ${order.instrument.id}, User: ${order.user}<br>Price: ${order.price}$, Quantity: ${order.qty}</span>\n` +
        "                </div>";
    document.getElementById("book-field").innerHTML += newdiv;
    listNotif(order.clientId);
}

function tradeOrder(order) {
    event.preventDefault();
    removeOrder(order.clientId);
    if (order.qty > 0) {
        orders[order.clientId] = order;
        let color = order.side === "BUY" ? "green" : "red";
        let newdiv =
            `                <div class=\"entry\" id="${order.clientId}">\n` +
            `                    <button type=\"submit\" class=\"cancel\" id=\"${order.user}-${order.clientId}" onclick="sendRemove(this.id)">x</button>\n` +
            `                    <span style=\"color: ${color}\">${order.side} Order </span><span>${order.clientId} - Instrument ID: ${order.instrument.id}, User: ${order.user}<br> Price: ${order.price}$, Quantity: ${order.qty}</span>\n` +
            "                </div>";
        document.getElementById("book-field").innerHTML += newdiv;
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
    document.getElementById("notif").style.display = "block";
    document.getElementById("notif").style.opacity = "1";
    document.getElementById("notif").style.borderColor = "#27af1d";
    document.getElementById("notif").style.width = "30%";
}

function removeNotif(id) {
    document.getElementById("notiftext").innerText = `Your listing removed, ID: ${id}`;
    document.getElementById("notif").style.display = "block";
    document.getElementById("notif").style.opacity = "1";
    document.getElementById("notif").style.borderColor = "#27af1d";
    document.getElementById("notif").style.width = "30%";
}

function tradeNotif(id) {
    document.getElementById("notiftext").innerText = `Your order ${id} has been traded!`;
    document.getElementById("notif").style.display = "block";
    document.getElementById("notif").style.opacity = "1";
    document.getElementById("notif").style.borderColor = "#27af1d";
    document.getElementById("notif").style.width = "30%";
}

function error() {
    document.getElementById("notiftext").innerText = "An error occurred, please try again later";
    document.getElementById("notif").style.display = "block";
    document.getElementById("notif").style.opacity = "1";
    document.getElementById("notif").style.borderColor = "#af1d1d";
    document.getElementById("notif").style.width = "30%";
}

function cancelError(order) {
    document.getElementById("notiftext").innerText = `There was a problem canceling your order: ${order.clientId}`;
    document.getElementById("notif").style.display = "block";
    document.getElementById("notif").style.opacity = "1";
    document.getElementById("notif").style.borderColor = "#af1d1d";
    document.getElementById("notif").style.width = "30%";
}

function orderError(order) {
    document.getElementById("notiftext").innerText = `There was a problem sending your order: ${order.clientId}`;
    document.getElementById("notif").style.display = "block";
    document.getElementById("notif").style.opacity = "1";
    document.getElementById("notif").style.borderColor = "#af1d1d";
    document.getElementById("notif").style.width = "30%";
}

function listError() {
    document.getElementById("notiftext").innerText = "Please enter valid order data";
    document.getElementById("notif").style.display = "block";
    document.getElementById("notif").style.opacity = "1";
    document.getElementById("notif").style.borderColor = "#af1d1d";
    document.getElementById("notif").style.width = "25%";
}

function showErrors() {
    document.getElementById("invalid-data").innerText = "Please enter valid: " + errors;
    document.getElementById("invalid-data").style.display = "block";
}

function fadeElem(id) {
    var tmp = document.getElementById(id).style;
    (function fade(){ (tmp.opacity -= 0.1) < 0 ? tmp.display = "none" : setTimeout(fade,40)})();
}