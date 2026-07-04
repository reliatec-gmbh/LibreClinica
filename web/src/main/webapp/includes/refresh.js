/* =============================================================================
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: https://www.libreclinica.org/download.html#headLicense
 *
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2026 LibreClinica
 * copyright (C) 2026 UMIN (University Hospital Medical Information Network)
 *
 * Author:  Yoshiteru Chiba
 * Notice:  Developed under a service-agreement contract with UMIN;
 *          copyright in this modification has been assigned to UMIN.
 *          The author retains moral rights inalienable under Article
 *          59 of the Japanese Copyright Act. See README for full
 *          attribution.
 *
 * Modifications:
 *   - Modification date: 2026-04-15 to 2026-04-27.
 *   - Replaced document.all['waitpage'] / document.all['mainpage'] with
 *     document.getElementById(); rewrote init() body for current browsers.
 *
 * License selection:
 *   The OpenClinica original was licensed under LGPL v2.1 or later.
 *   Per LGPL v2.1 section 13, this work selects version 3.0 of the GNU
 *   Lesser General Public License, matching the upstream LibreClinica
 *   distribution license.
 * ============================================================================= */

function init() {
	var waitpage = document.getElementById('waitpage');
	var mainpage = document.getElementById('mainpage');
	if (waitpage) waitpage.style.visibility = 'hidden';
	if (mainpage) mainpage.style.visibility = 'visible';
}
