// ==UserScript==
// @name     Event Engine UI enhancements
// @description Event Engine UI enhancements
// @version  2
// @grant    unsafeWindow
// @include  http://192.168.86.20/*
// @require  https://code.highcharts.com/stock/highstock.js
// @require  https://code.highcharts.com/stock/modules/data.js
// @require  https://code.highcharts.com/stock/highcharts-more.js
// @require  https://code.highcharts.com/stock/modules/exporting.js
// @require  https://momentjs.com/downloads/moment.js
// @run-at document-end
// ==/UserScript==

(function () {
  if (!document.title.includes('Hubitat')) {
    document.title = `Hubitat - ${document.title}`;
  }

  var nav = document.getElementsByTagName("nav")[0];
  if (nav) {
    if (![...nav.getElementsByClassName('mdl-navigation__link')].find(item => item.innerText.includes('Event Engine'))) {
      var link = document.createElement("a");
      link.classList.add("mdl-navigation__link");
      link.href = "/installedapp/list?display=eventengine";
      link.innerHTML = "<i class=\"material-icons he-apps_21\"></i>Event Engine";

      nav.insertBefore(link, nav.childNodes[8]);
    }
  }

  if (window.location.href.endsWith('/installedapp/list?display=eventengine')) {
    document.title = 'Hubitat - Event Engine';
    
    document.getElementsByClassName('mdl-layout-title')[0].firstChild.innerHTML = 'Event Engine';
    // If using the beta 2.3.1.+, comment out the line above and uncomment the line below.
    //document.getElementsByClassName('mdl-layout__header-row')[0].firstChild.nextSibling.innerHTML = 'Event Engine';

    nav.getElementsByClassName('is-active')[0].classList.remove('is-active');
    link.classList.add('is-active');

    var appTable = document.getElementById('app-table');
    var divs = appTable.getElementsByClassName('app-row-link');
    var eventEngine = [...divs].find(div => div.children[0].innerText == 'Event Engine');
    var eventEngineId = eventEngine.parentElement.getAttribute('data-id');

    var buttonsContainer = document.getElementById('buttonsContainer');
    buttonsContainer.children[0].remove();
    buttonsContainer.children[0].innerHTML = `<a href="/installedapp/createchild/BPTWorld/Event Engine Cog/parent/${eventEngineId}" class="btn btn-default btn-lg btn-block hrefElem mdl-button--raised mdl-shadow--2dp" style="text-align:left">
                                            <span class="he-add_2"></span> <span class="pre">Create New Cog</span>
                                        </a>`;

    var cogs = [...eventEngine.parentElement.parentElement.children].slice(1).map(cog => cog.children[2]);
    //appTable.children[0].children[0].children[1].remove();
    appTable.children[0].style.display = 'none';

    var tbody = appTable.children[1];
    tbody.innerHTML = '';
    var lastRoom = '';
    cogs.forEach(cog => {
      if (cog) {
        var cogName = cog.children[0].innerText;
        var splitter = cogName.includes('-') ? '-' : ' ';
        var room = cogName.split(splitter)[0].trim();

        if (room != lastRoom) {
          var trRoom = document.createElement("tr")
          trRoom.classList.add("group");
          trRoom.innerHTML = `<td style="display: none"></td><td><b>${room}</b></td>`;
          tbody.append(trRoom);
        }
        lastRoom = room;

        var tr = document.createElement("tr")
        tr.innerHTML = `<td style="display: none"></td><td><div style="padding-left: 15px">${cog.innerHTML}</div></td>`;
        tbody.append(tr);
      }
    });
  }

})();

function onRemove(element, callback) {
  try {
    const obs = new MutationObserver(mutations => {
      for (const mutation of mutations) {
        for (const el of mutation.removedNodes) {
          var parent = element;
          while (parent) {
            if (el === parent) {
              obs.disconnect();
              callback();
            }
            parent = parent.parentElement;
          }
        }
      }
    });
    obs.observe(document.body, {
      subtree: true,
      childList: true,
    });
  } catch (ex) {
    alert(ex);
  }
}
