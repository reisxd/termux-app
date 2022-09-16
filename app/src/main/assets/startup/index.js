const WS_URI = `${window?.location?.protocol === 'https:' ? 'wss' : 'ws'}://localhost:65469`;
const ws = new WebSocket(WS_URI);

let lastPage = "home";

function changeHeader(_head, _desc) {
  let head = document.getElementById("head");
  let desc = document.getElementById("desc");
  head.innerText = _head;
  desc.innerText = _desc;
}

function switchTo(page) {
  if (page == "home")
    document.querySelector("#goToHome").display = "none";
  else
    document.querySelector("#goToHome").display = "block";
  document.getElementById(lastPage)
    .style.display = "none";
  document.getElementById(page)
    .style.display = "block";
  lastPage = page;
}

function setGoToHomeState(clickable) {
  const b = document.querySelector("#goToHome");
  if (!clickable)
    b.setAttribute("disabled", "disabled");
  else
    b.removeAttribute("disabled");
}

function run() {
  switchTo("next-step");
  setGoToHomeState(false);
  changeHeader("Preflight", "Checking if ReVanced Builder is installed");
  ws.send(JSON.stringify({ action: "preflight" }));
}

function reinstall() {
  switchTo("next-step");
  // TODO: Fix desc
  changeHeader("Reinstalling ReVanced Builder", "Reinstallation allows the Builder to blahblahblah");
}

function update() {
  switchTo("next-step");
  // TODO: Fix desc
  changeHeader("Updating ReVanced Builder", "blahblahblah");
}

function about() {
  switchTo("about");
  changeHeader("Credits", "");
  document.getElementById("content--wrapper").style.backgroundColor = "transparent";
  // copy the about page logic
  const contributors = [
    { username: 'reisxd', name: 'Reis' },
    { username: 'shrihanDev' },
    { username: 'Elijah629' },
    { username: 'victor141516', name: 'Víctor Fernández' },
    { username: 'xemulat' },
    { username: 'viperML', name: 'Fernando Ayats' },
    { username: 'SebiAi' },
    { username: 'CnC-Robert', name: 'Robert' },
    { username: 'Aunali321' },
    { username: 'Katrovsky', name: 'Кирилл Катровский' },
    { username: 'JavaCafe01', name: 'Gokul Swaminathan' },
    { username: 'naserkesetovic', name: 'Naser Kešetović' },
    { username: 'j4k0xb' },
    { username: 'sakya', name: 'Paolo Iommarini' }
  ];
  for (const e of contributors)
    document.getElementById(
      'revanced-builder'
    ).innerHTML += `<span class="member"><span class="member-image" style="background-image:url('https://github.com/${
      e.username
    }.png')"></span><span class="member-name"><a href="https://github.com/${
      e.username
    }">${e.name || e.username}</a></span></span>`;
}

function goToHome () {
  if (lastPage !== "home") {
    switchTo("home");
    changeHeader('ReVanced Builder', 'Select one of the options to continue.');
  }
}

function exit () {
  // TODO: send 'exit' message here
  document.getElementById(lastPage).style.display = "none";
  document.getElementById("content--wrapper").style.backgroundColor = "transparent";
  document.getElementsByTagName("footer")[0].style.display = "none";
  changeHeader("Exited", "The app should automatically close. If it doesn't, close the app manually.");
}

function appendLogOrProgress ({ type, msg }) {
  const log = document.getElementsByClassName("log")[0];
  const logLine document.createElement("span");
  if (type === "progress") {
    const prog = document.getElementsByTagName("progress")[0];
    if (parseInt(msg) < 100) {
      prog.style.display = "block";
      prog.setAttribute("value", msg);
    } else {
      prog.style.display = "none";
    }
    return;
  }
  if (type === "error") setGoToHomeState(true);

  logLine.setAttribute("class", `log-line ${type}`);
  logLine.innerHTML = `<strong>[${type.toUpperCase()}]</strong> ${msg}`;
  log.appendChild(logLine);
}

ws.onmessage = ({ data }) => appendLogOrProgress(JSON.parse(data));
