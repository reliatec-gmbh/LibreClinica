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
 *   - Updated jQuery script reference from "jquery-1.9.1.min.js" to
 *     "jquery-3.7.1.min.js" to match the upgraded jQuery bundle.
 *
 * License selection:
 *   The OpenClinica original was licensed under LGPL v2.1 or later.
 *   Per LGPL v2.1 section 13, this work selects version 3.0 of the GNU
 *   Lesser General Public License, matching the upstream LibreClinica
 *   distribution license.
 * ============================================================================= */

head.js( app_contextPath + "/includes/jmesa/jquery-3.7.1.min.js",
         app_contextPath + "/js/lib/jquery.tmpl.js",
         app_contextPath + "/js/lib/underscore-min.js",
         app_contextPath + "/js/lib/backbone-min.js",
         app_contextPath + "/js/lib/date.format.js",           
         app_contextPath + "/js/util.js",
         app_contextPath + "/js/com/openclinica/renderer/RenderUtil.js",           
         app_contextPath + "/js/app.js",           
         app_contextPath + "/js/com/openclinica/renderer/ODMRenderer.js",           
         app_contextPath + "/js/com/openclinica/renderer/StudyDataLoader.js",           
         app_contextPath + "/js/com/openclinica/renderer/StudyRenderer.js",           
         app_contextPath + "/js/com/openclinica/renderer/FormDefRenderer.js",           
         app_contextPath + "/js/com/openclinica/renderer/StudyEventDefRenderer.js",           
         app_contextPath + "/js/com/openclinica/renderer/StudySubjectDefRenderer.js",           
         app_contextPath + "/js/com/openclinica/renderer/ItemDefRenderer.js",           
         app_contextPath + "/js/com/openclinica/renderer/PageHeaderRenderer.js",           
         app_contextPath + "/js/com/openclinica/renderer/ParseUtil.js"          
       );