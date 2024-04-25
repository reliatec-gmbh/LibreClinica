
const data = {
    "9": {
        "SPK": [1001, 1340],
        "IPK": [3001, 3040],
        "PG": [3032448512, 3032448851],
        "RLB": [3032449712, 3032450714],
        "RS": [1001, 2870]
    },
    "22": {
        "SPK": [1341, 1360],
        "IPK": [3041, 3050],
        "PG": [3032448852, 3032448871],
        "RLB": [3032450715, 3032450773],
        "RS": [2871, 2980]
    },
    "26": {
        "SPK": [1361, 1400],
        "IPK": [3051, 3070],
        "PG": [3032448872, 3032448911],
        "RLB": [3032450774, 3032450891],
        "RS": [2981, 3200]
    },
    "30": {
        "SPK": [1401, 1620],
        "IPK": [3071, 3110],
        "PG": [3032448912, 3032449131],
        "RLB": [3032450892, 3032451540],
        "RS": [3201, 4410]
    },
    "33": {
        "SPK": [1621, 1720],
        "IPK": [3111, 3150],
        "PG": [3032449132, 3032449231],
        "RLB": [3032451541, 3032451835],
        "RS": [4411, 4960]
    },
    "45": {
        "SPK": [1721, 1820],
        "IPK": [3151, 3190],
        "PG": [3032449232, 3032449331],
        "RLB": [3032451836, 3032452130],
        "RS": [4961, 5510]
    },
    "63": {
        "SPK": [1821, 1840],
        "IPK": [3191, 3200],
        "PG": [3032449332, 3032449351],
        "RLB": [3032452131, 3032452189],
        "RS": [5511, 5620]
    },
    "64": {
        "SPK": [1841, 1860],
        "IPK": [3201, 3210],
        "PG": [3032449352, 3032449371],
        "RLB": [3032452190, 3032452248],
        "RS": [5621, 5730]
    },
    "64A": {
        "SPK": [1861, 1880],
        "IPK": [3211, 3220],
        "PG": [3032449372, 3032449391],
        "RLB": [3032452249, 3032452307],
        "RS": [5731, 5840]
    },
    "67": {
        "SPK": [1881, 1960],
        "IPK": [3221, 3260],
        "PG": [3032449392, 3032449463],
        "RLB": [3032452308, 3032452543],
        "RS": [5841, 6225]
    },
    "74": {
        "SPK": [1961, 2040],
        "IPK": [3261, 3300],
        "PG": [3032449464, 3032449545],
        "RLB": [3032452544, 3032452779],
        "RS": [6226, 6665]
    },
    "75": {
        "SPK": [2041, 2100],
        "IPK": [3301, 3330],
        "PG": [3032449546, 3032449607],
        "RLB": [3032452780, 3032452956],
        "RS": [6666, 6995]
    },
    "76": {
        "SPK": [2101, 2180],
        "IPK": [3331, 3370],
        "PG": [3032449608, 3032449679],
        "RLB": [3032452957, 3032453192],
        "RS": [6996, 7380]
    },
    "77": {
        "SPK": [2181, 2220],
        "IPK": [3371, 3390],
        "PG": [3032449680, 3032449711],
        "RLB": [3032453193, 3032453311],
        "RS": [7381, 7600]
    }
}


function search (site, clasification) {
  if (data[site] && data[site][clasification]) {
      return data[site][clasification];
  } else {
      return "Specified site or classification not found.";
  }
}

function generateOptions(startRange, endRange){
  const selectElement = document.getElementById('tbtclabels');
  for (let i = startRange; i <= endRange; i++) {
      const option = document.createElement("option");
      option.value = i;
      option.textContent = i;
      selectElement.appendChild(option);
  }
}

function getParameters(currentUrl){
  const url = new URL(currentUrl);
  const params = new URLSearchParams(url.search);
  const site = params.get('site');
  const cat = params.get('cat');
  const label = params.get('labelid');
  var SiteElement = document.getElementById('titlesite');
      SiteElement.textContent = 'Site ' + site;
  const rangeFound = search(site, cat);
  if (Array.isArray(rangeFound)) {
    console.log('Range and category Found:' + rangeFound[0] + ' ' + rangeFound[1]);
    generateOptions(rangeFound[0], rangeFound[1]);
    return label;
  } else {
    console.log(rangeFound);
  }
}