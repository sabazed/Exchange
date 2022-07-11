var wsocket = new WebSocket("ws://localhost:8080/Exchange/order");

wsocket.onmessage = function (evt) {
    document.getElementById("overlay").style.display = "none";
    let data = JSON.parse(evt.data);
    if (data.status === "removed") {
        removeOrder(data.id);
        removeNotif(data.id);
    }
    else if (data.status === "trade") {
        tradeNotif(data.sold, data.bought);
        removeOrder(data.sold);
        removeOrder(data.bought);
    }
    else if (data.status === "listing") {
        addOrder(data.user, data.order);
    }
    else if (data.status === "fremove") {
        removeNotif();
    }
    else if (data.status === "forder") {
        listError();
    }

}

function listNotif(id) {
    document.getElementById("notif").innerText = `New order listed, entry ID: ${id}`;
    document.getElementById("overlay").style.display = "block";
    document.getElementById("overlay").style.borderColor = "#27af1d";
    document.getElementById("overlay").style.width = "30%";
}

function removeNotif(id) {
    document.getElementById("notif").innerText = `Your listing removed, ID: ${id}`;
    document.getElementById("overlay").style.display = "block";
    document.getElementById("overlay").style.borderColor = "#27af1d";
    document.getElementById("overlay").style.width = "30%";
}

function tradeNotif(sold, bought) {
    document.getElementById("notif").innerText = `Orders traded! IDs: ${sold} and ${bought}`;
    document.getElementById("overlay").style.display = "block";
    document.getElementById("overlay").style.borderColor = "#27af1d";
    document.getElementById("overlay").style.width = "30%";
}

function removeError() {
    document.getElementById("notif").innerText = "An error occurred, please try again later";
    document.getElementById("overlay").style.display = "block";
    document.getElementById("overlay").style.borderColor = "#af1d1d";
    document.getElementById("overlay").style.width = "30%";
}

function listError() {
    document.getElementById("notif").innerText = "Please enter valid data";
    document.getElementById("overlay").style.display = "block";
    document.getElementById("overlay").style.borderColor = "#af1d1d";
    document.getElementById("overlay").style.width = "25%";
}

function sendOrder() {
    event.preventDefault();
    let user = document.getElementById("user").value;
    let instr = document.getElementById("instrument").value;
    let price = document.getElementById("price").value;
    let qty = document.getElementById("qty").value;
    let side = document.getElementById("side").value;
    wsocket.send(`{"status":"order", "data":{"user":"${user}", "instr":"${instr}", "price":"${price}", "qty":"${qty}", "side":"${side}"}}`);
}

function addOrder(user, data) {
    event.preventDefault();
    let color = data.side === "Buy" ? "green" : "red";
    let newdiv =
        `                <div class=\"entry\" id="${data.id}">\n` +
        `                    <button type=\"submit\" class=\"cancel\" id=\"${user}-${data.id}" onclick="sendRemove(this.id)">x</button>\n` +
        `                    <span style=\"color: ${color}\">${data.side} Order </span><span>- Instrument ID: ${data.instr},<br> Price: ${data.price}$, Quantity: ${data.qty}</span>\n` +
        "                </div>";
    document.getElementById("book-field").innerHTML += newdiv;
    listNotif(data.id);
}

function sendRemove(bid) {
    event.preventDefault();
    let arr = bid.split('-');
    wsocket.send(`{"status":"remove", "user":"${arr[0]}", "id":"${arr[1]}"}`);
}


function removeOrder(bid) {
    event.preventDefault();
    document.getElementById(bid).outerHTML = "";
}