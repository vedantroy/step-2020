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
  for (const [key, val] of data) {
    if (key === "max-comments") {
      const newVal = parseInt(val);
      if (newVal !== maxComments) {
        maxComments = newVal;
        getComments();
      }
    }
  }
});

document.getElementById('delete-comments').addEventListener('click', async (_) => {
  await fetch(`delete-data`, {method: 'POST'})
  getComments()
})

async function getComments() {
  const resp = await fetch(`data?max_comments=${maxComments}`);
  const commentsArray = await resp.json();
  const commentsDiv = document.getElementById("comments");
  commentsDiv.innerHTML = "";
  for (const comment of commentsArray) {
    const p = document.createElement("li");
    p.textContent = comment;
    commentsDiv.appendChild(p);
  }
}

getComments()