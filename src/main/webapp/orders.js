var orders = {};
var wsocket = new WebSocket("ws://localhost:8080/Exchange/order");

wsocket.onmessage = function (evt) {
    document.getElementById("notif").style.display = 'none';
    console.log(evt.data);
    let data = JSON.parse(evt.data);
    if (data.status === "Cancel") {
        removeOrder(data.id);
    }
    else if (data.status === "Trade") {
        tradeOrder(data);
    }
    else if (data.status === "List") {
        addOrder(data);
    }
    else if (data.status === "Fail") {
        listError();
    }

}

wsocket.onopen = function (evt) {
    for (let order in Object.values(orders)) {
        addOrder(order);
    }
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

function sendOrder() {
    event.preventDefault();
    let json = {};
    json.status = "Order";
    json.user = document.getElementById("user").value;
    json.instr = document.getElementById("instrument").value;
    json.price = document.getElementById("price").value;
    json.qty = document.getElementById("qty").value;
    json.side = document.getElementById("side").value;
    json.id = -1;
    console.log(json);
    wsocket.send(JSON.stringify(json));
}

function sendRemove(bid) {
    event.preventDefault();
    let arr = bid.split('-');
    let json = orders[arr[1]];
    console.log(json);
    wsocket.send(JSON.stringify(json));
}

function addOrder(data) {
    event.preventDefault();
    orders[data.id] = data;
    let color = data.side === "BUY" ? "green" : "red";
    let newdiv =
        `                <div class=\"entry\" id="${data.id}">\n` +
        `                    <button type=\"submit\" class=\"cancel\" id=\"${data.user}-${data.id}" onclick="sendRemove(this.id)">x</button>\n` +
        `                    <span style=\"color: ${color}\">${data.side} Order </span><span>- Instrument ID: ${data.instrument.id},<br> Price: ${data.price}$, Quantity: ${data.qty}</span>\n` +
        "                </div>";
    document.getElementById("book-field").innerHTML += newdiv;
    listNotif(data.id);
}

function tradeOrder(data) {
    event.preventDefault();
    removeOrder(data.id);
    if (data.qty > 0) {
        let color = data.side === "BUY" ? "green" : "red";
        let newdiv =
            `                <div class=\"entry\" id="${data.id}">\n` +
            `                    <button type=\"submit\" class=\"cancel\" id=\"${data.user}-${data.id}" onclick="sendRemove(this.id)">x</button>\n` +
            `                    <span style=\"color: ${color}\">${data.side} Order </span><span>- Instrument ID: ${data.instrument.id},<br> Price: ${data.price}$, Quantity: ${data.qty}</span>\n` +
            "                </div>";
        document.getElementById("book-field").innerHTML += newdiv;
    }
    tradeNotif(data.id);
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
    document.getElementById("notif").style.borderColor = "#27af1d";
    document.getElementById("notif").style.width = "30%";
}

function removeNotif(id) {
    document.getElementById("notiftext").innerText = `Your listing removed, ID: ${id}`;
    document.getElementById("notif").style.display = "block";
    document.getElementById("notif").style.borderColor = "#27af1d";
    document.getElementById("notif").style.width = "30%";
}

function tradeNotif(id) {
    document.getElementById("notiftext").innerText = `Your order ${id} has been traded!`;
    document.getElementById("notif").style.display = "block";
    document.getElementById("notif").style.borderColor = "#27af1d";
    document.getElementById("notif").style.width = "30%";
}

// function removeError() {
//     document.getElementById("notiftext").innerText = "An error occurred, please try again later";
//     document.getElementById("notif").style.display = "block";
//     document.getElementById("notif").style.borderColor = "#af1d1d";
//     document.getElementById("notif").style.width = "30%";
// }

function listError() {
    document.getElementById("notiftext").innerText = "Please enter valid data";
    document.getElementById("notif").style.display = "block";
    document.getElementById("notif").style.borderColor = "#af1d1d";
    document.getElementById("notif").style.width = "25%";
}

function fadeElem(id) {
    document.getElementById(id).style.opacity = 0;
}