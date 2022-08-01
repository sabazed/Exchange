let orders = {};
let order_id = 0;
let request_id = 0;
let loaded = false;
let wsocket = new WebSocket("ws://localhost:8080/Exchange/order");
fadeElem("overlay"); // Hide the notification

wsocket.onmessage = function (evt) {
    console.log(evt.data);
    let json = JSON.parse(evt.data);
    if (json.type === "Listing") {
        addOrder(json);
    }
    else if (json.type === "Trade") {
        tradeOrder(json);
    }
    else if (json.type === "Remove") {
        removeOrder(json.clientId);
    }
    else if (json.type === "Fail") {
        if (json.status === "OrderFail") {
            orderError(json);
        }
        else if (json.status === "CancelFail") {
            cancelError(json);
        }
        else if (json.status === "RequestFail") {
            requestError();
            wsocket.close();
        }
        else {
            showError(json.status);
        }
    }
    else if (json.type === "InstrumentDataResponse") {
            loadInstruments(json.instruments);
            let newjson = {};
            newjson.type = "MarketDataRequest";
            newjson.clientId = request_id++;
            console.log(newjson);
            wsocket.send(JSON.stringify(newjson));
    }
    else if (json.type === "MarketDataResponse") {
            updateMarketData(json.updates);
    }
}

wsocket.onopen = function (evt) {
    let json = {};
    json.type = "InstrumentDataRequest";
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
    let instrumentSelect = document.getElementById("instrument");
    let marketData = document.getElementById("market-field");
    for (let instrument of instruments) {
        instrumentSelect.innerHTML += `<option value=${instrument.id}>${instrument.name}</option>`;
        marketData.innerHTML +=
            `            <div class=\"market-entry\" id=\"$instrument{instrument.id}\">` +
            `                <p>Instrument ${instrument.id}:` +
            `                    <br>${instrument.name}<br>` +
            `                    <span id="instrument${instrument.id}-price">Buy: 0<br>Sell: 0<br>Last Trade: 0</span>` +
            `                </p>` +
            `            </div>`;
    }
}

function updateMarketData(entries) {
    for (let entry of entries) {
        document.getElementById(`instrument${entry.instrument.id}-price`).innerHTML = `Buy: ${entry.buy}<br>Sell: ${entry.sell}<br>Last Trade: ${entry.lastTrade}`;
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
        `                <div class=\"entry\" id="order${order.clientId}">\n` +
        `                    <button type=\"submit\" class=\"cancel\" id=\"order${order.user}-${order.clientId}" onclick="sendRemove(this.id)">x</button>\n` +
        `                    <span style=\"color: ${color}\">${order.side} Order </span><span>${order.clientId} - Instrument: ${order.instrument.name}, User: ${order.user}<br>Price: ${order.price}$, Quantity: ${order.qty}</span>\n` +
        "                </div>";
    document.getElementById("book-field").innerHTML += newdiv;
    listNotif(order.clientId);
}

function tradeOrder(trade) {
    event.preventDefault();
    let order = orders[trade.clientId];
    if (order === undefined) {
        tradeNotif(trade.clientId);
        return;
    }
    order.qty = trade.qty;
    if (order.qty > 0) {
        let color = order.side === "BUY" ? "green" : "red";
        document.getElementById(`order${order.clientId}`).innerHTML =
            `                    <button type=\"submit\" class=\"cancel\" id=\"${order.user}-${order.clientId}" onclick="sendRemove(this.id)">x</button>\n` +
            `                    <span style=\"color: ${color}\">${order.side} Order </span><span>${order.clientId} - Instrument: ${order.instrument.name}, User: ${order.user}<br> Price: ${order.price}$, Quantity: ${order.qty}</span>\n`;
    } else {
        removeOrder(order.clientId);
    }
    tradeNotif(order.clientId);
}

function removeOrder(bid) {
    event.preventDefault();
    document.getElementById(`order${bid}`).remove();
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