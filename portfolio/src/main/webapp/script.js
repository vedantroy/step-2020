// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

function createRow({ description, imageSrc, link }) {
  const template = document.getElementsByTagName("template")[0];
  const clone = template.content.cloneNode(true);
  clone.getElementById("project-description").textContent = description;
  clone.getElementById("project-image").src = imageSrc;
  clone.getElementById("link").href = link;
  return clone;
}
const content = document.getElementById("content");
content.appendChild(
  createRow({
    description:
      "A Typescript compiler-extension/macro that generates validation code to verify types at runtime.",
    imageSrc: "/images/macro.png",
    link: "https://github.com/vedantroy/typecheck.macro",
  })
);
content.appendChild(
  createRow({
    description:
      "A keyboard that lets users send anime GIFs. (The link is broken because I need to update the privacy policy or something).",
    imageSrc: "/images/anime-keyboard.png",
    link:
      "https://play.google.com/store/apps/details?id=com.vedantroy.animefacekeyboard",
  })
);
content.appendChild(
  createRow({
    description: "My prior (very out of date) portfolio site ;)",
    imageSrc: "/images/portfolio.png",
    link: "https://vedantroy.github.io/",
  })
);

let maxComments = 3;
const form = document.getElementById("max-comments-form");
form.addEventListener("submit", (e) => {
  e.preventDefault();
  const data = new FormData(form);
  const newMaxComments = parseInt(data.get("max-comments"));
  if (newMaxComments !== maxComments && newMaxComments !== NaN) {
    maxComments = parseInt(newMaxComments);
    getComments(maxComments);
  }
});

document
  .getElementById("delete-comments")
  .addEventListener("click", async (_) => {
    await fetch(`delete-data`, { method: "POST" });
    getComments();
  });

async function getComments(numComments) {
  const resp = await fetch(`data?max_comments=${numComments}`);
  const commentsArray = await resp.json();
  const commentsDiv = document.getElementById("comments");
  commentsDiv.innerHTML = "";
  for (const comment of commentsArray) {
    const { text, url } = comment;
    const p = document.createElement("p");
    p.textContent = text;
    commentsDiv.appendChild(p);
    if (url != null) {
      const img = document.createElement("img");
      img.src = url;
      img.classList.add("image");
      commentsDiv.appendChild(img);
    }
  }
}

getComments(maxComments);

async function initFileUpload() {
  const resp = await fetch("/blobstore-upload-url");
  const url = await resp.text();
  const submitCommentForm = document.getElementById("submit-comment-form");
  submitCommentForm.action = url;
  submitCommentForm.classList.remove("hidden");
}

initFileUpload();

google.charts.load("current", { packages: ["corechart"] });
google.charts.setOnLoadCallback(drawChart);

async function drawChart() {
  const chart = new google.visualization.DataTable();
  const data = await fetch("/population-data");
  const json = await data.json();
  console.log(json);
  chart.addColumn("string", "Year");
  chart.addColumn("number", "Population");
  for (let i = json.length - 1; i > 0; i--) {
    const entry = json[i];
    const year = entry.date;
    const population = parseInt(entry.value);
    chart.addRow([year, population]);
  }
  const options = {
    title: "US Population Per Year",
    width: 500,
    height: 500,
  };
  const chartElement = new google.visualization.LineChart(
    document.getElementById("chart")
  );
  chartElement.draw(chart, options);
}
