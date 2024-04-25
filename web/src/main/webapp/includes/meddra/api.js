
// FUNCTION TO GET MEDDRA TOKEN
const getToken = async () => {
  const body = new URLSearchParams();
  body.append('grant_type', token_grant_type);
  body.append('username', token_username);
  body.append('password', token_password);
  body.append('scope', token_scope);
  body.append('client_id', token_client_id);

  const headers = {
    'Content-Type': 'application/x-www-form-urlencoded'
  };

  try {
    const response = await fetch(token_url, {
      method: 'POST',
      headers: headers,
      body: body
    });

    if (response.ok) {
      const data = await response.json();
      return data.access_token;
    } else {
      console.error('Failed to get token:', response.status);
      return null;
    }
  } catch (error) {
    console.error('Error:', error);
    return null;
  }
};

// MEDDRA SEARCH FUNCTION
const search = async (vlanguage, vsearchterm, vbview, vversion) => {
  const accessToken = await getToken();
  if (!accessToken) {
    console.error('No access token available');
    return;
  }

  const requestBody = { bview: vbview, rsview: 'release', language: vlanguage, stype: 1, addlangs: [], filters: [], version: vversion, searchterms: [{ searchtype: 0, searchterm: vsearchterm, searchlogic: 0 }], kana: true, idiacritical: true, synonym: true, contains: true, soc: true, hlgt: true, hlt: true, pt: true, llt: true, smq: true, skip: 0, take: 0, separator: 2 };
  try {
    const response = await fetch(search_url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + accessToken
      },
      body: JSON.stringify(requestBody)
    });

    if (response.ok) {
      const data = await response.json();
      displayDataInTable(data);
    } else {
      console.error('Failed to fetch data:', response.status);
      const apiResult = document.getElementById('apiResult');
      apiResult.innerHTML = '<p>Error: Failed to fetch data</p>';
    }
  } catch (error) {
    console.error('Error:', error);
    const apiResult = document.getElementById('apiResult');
    apiResult.innerHTML = '<p>Error: ' + error + '</p>';
  }
};

// FUNCTION TO OBTAIN THE MEDDRA LANGUAGE LIST
const language_ = async () => {
  const accessToken = await getToken();
  if (!accessToken) {
    console.error('No access token available');
    return;
  }

  const langt = 1;
  const url = language_url + '/' + encodeURIComponent(langt);
  try {
    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + accessToken
      }

    });
    if (response.ok) {
      const data = await response.json();
      //console.log("Primer lenguaje:" , data);
      await displayLanguageinSelect(data);
      changeRelease();
    } else {
      console.error('Failed to fetch data:', response.status);
      const apiResult = document.getElementById('apiResult');
      apiResult.innerHTML = '<p>Error: Failed to fetch data</p>';
    }
  } catch (error) {
    console.error('Error:', error);
    const apiResult = document.getElementById('apiResult');
    apiResult.innerHTML = `<p>Error: ${error}</p>`;
  }
};

// GET API RELEASE DATA
const release = async () => {
  const accessToken = await getToken();
  if (!accessToken) {
    console.error('No access token available');
    return;
  }

  try {
    const response = await fetch(release_url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + accessToken
      }

    });
    if (response.ok) {
      const data = await response.json();
      const ordenado = data.sort((a, b) => b.meddraVersion - a.meddraVersion);
      //console.log("releases:", data);
      await displayReleaseinSelect(ordenado);
      language_();
    } else {
      console.error('Failed to fetch data:', response.status);
      const apiResult = document.getElementById('apiResult');
      apiResult.innerHTML = '<p>Error: Failed to fetch data</p>';
    }
  } catch (error) {
    console.error('Error:', error);
    const apiResult = document.getElementById('apiResult');
    apiResult.innerHTML = `<p>Error: ${error}</p>`;
  }
};

//SHOW IN A TABLE THE SEARCH PERFORMED IN MEDDRA
const displayDataInTable = (data) => {
  const tableBody = document.getElementById('tableBody');
  tableBody.innerHTML = '';
  data.forEach(item => {
    const row = document.createElement('tr');
    const pcode = document.createElement('td');
    const category = document.createElement('td');
    const code = document.createElement('td');
    const name = document.createElement('td');
    const abbrev = document.createElement('td');
    const level = document.createElement('td');

    pcode.textContent = item.pcode;
    category.textContent = item.category;
    code.textContent = item.code;
    name.textContent = item.name;
    abbrev.textContent = item.abbrev;
    level.textContent = item.level;

    row.appendChild(pcode);
    row.appendChild(category);
    row.appendChild(code);
    row.appendChild(name);
    row.appendChild(abbrev);
    row.appendChild(level);

    row.addEventListener('click', function () {
      window.opener.postMessage(item, '*');
      localStorage.setItem('code', item.code);
      localStorage.setItem('name', item.name);
      console.log("code:", item.code);
      console.log("name:", item.name);
      // cerrarPopup();
      //window.close();
    });
    tableBody.appendChild(row);
  });
};

//SHOW IN A SELECT THE LANGUAGE SEARCH PERFORMED IN MEDDRA
const displayLanguageinSelect = async (data) => {
  const vselect = document.getElementById('language');
  data.forEach(item => {
    const voption = document.createElement('option');
    voption.value = item.langname;
    voption.textContent = item.langname;
    voption.setAttribute("data-rel1", item.firstversion);
    voption.setAttribute("data-rel2", item.lastversion);
    voption.selected = item.langname == "English" ? true : false;
    vselect.appendChild(voption);
  });
};

//SHOW IN A SELECT THE VERSION SEARCH PERFORMED IN MEDDRA
const displayReleaseinSelect = async (data) => {
  const vselect = document.getElementById('version');
  data.forEach(item => {
    const voption = document.createElement('option');
    voption.value = item.meddraVersion;
    voption.textContent = item.meddraVersion;
    voption.setAttribute("data-parent", item.meddraLang);
    vselect.appendChild(voption);
  });
};

//GET THE FORM DATA 
const getData = () => {
  const language = document.getElementById('language').value;
  const version = document.getElementById('version').value;
  const searchterm = document.getElementById('searchterm').value;
  const bview = document.getElementById('bview').value;
  search(language, searchterm, bview, version);
};

//FILTER AVAILABLE VERSIONS FOR EACH LANGUAGE
const changeRelease = () => {
  const version = document.getElementById('version');
  const language = document.getElementById('language');
  const selectedOption = language.value;
  const versionOption = version.getElementsByTagName('option');

  for (let i = 0; i < versionOption.length; i++) {
    const opcion = versionOption[i];
    const parent = opcion.getAttribute('data-parent');

    if (parent === selectedOption) {
      opcion.style.display = '';
    } else {
      opcion.style.display = 'none';
    }
  }

  version.selectedIndex = -1;
  if (version.length > 0) {
    version.selectedIndex = 0;
  }

};

document.addEventListener('DOMContentLoaded', function () {
  const language = document.getElementById('language');
  release(); // CALL RELEASE API

  language.addEventListener('change', function () {
    changeRelease();
  });

});

