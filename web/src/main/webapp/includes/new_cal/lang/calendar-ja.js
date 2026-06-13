// ** I18N

// Calendar JA (Japanese) language
// Original calendar author: Mihai Bazon, <mihai_bazon@yahoo.com>
// Japanese translation: Yoshiteru Chiba, 2026
//   (developed under a service-agreement contract with UMIN
//    <https://www.umin.ac.jp/>; copyright in the translation has been
//    assigned to UMIN)
// Encoding: UTF-8
// Distributed under the same terms as the calendar itself (GNU LGPL).

// full day names
Calendar._DN = new Array
("日曜日",
 "月曜日",
 "火曜日",
 "水曜日",
 "木曜日",
 "金曜日",
 "土曜日",
 "日曜日");

// short day names
Calendar._SDN = new Array
("日",
 "月",
 "火",
 "水",
 "木",
 "金",
 "土",
 "日");

// First day of the week. "0" means display Sunday first, "1" means display
// Monday first, etc.
Calendar._FD = 0;

// full month names
Calendar._MN = new Array
("1月",
 "2月",
 "3月",
 "4月",
 "5月",
 "6月",
 "7月",
 "8月",
 "9月",
 "10月",
 "11月",
 "12月");

// short month names
Calendar._SMN = new Array
("1月",
 "2月",
 "3月",
 "4月",
 "5月",
 "6月",
 "7月",
 "8月",
 "9月",
 "10月",
 "11月",
 "12月");

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = "カレンダーについて";

Calendar._TT["ABOUT"] =
"DHTML Date/Time Selector\n" +
"(c) dynarch.com 2002-2005 / Author: Mihai Bazon\n" + // don't translate this this ;-)
"For latest version visit: http://www.dynarch.com/projects/calendar/\n" +
"Distributed under GNU LGPL.  See http://gnu.org/licenses/lgpl.html for details." +
"\n\n" +
"日付の選択:\n" +
"- \xab、\xbb ボタンで年を選択します\n" +
"- " + String.fromCharCode(0x2039) + "、" + String.fromCharCode(0x203a) + " ボタンで月を選択します\n" +
"- ボタンを押し続けると素早く選択できます。";
Calendar._TT["ABOUT_TIME"] = "\n\n" +
"時刻の選択:\n" +
"- 時刻の各部分をクリックすると増加します\n" +
"- Shift を押しながらクリックすると減少します\n" +
"- クリックしたままドラッグすると素早く変更できます。";

Calendar._TT["PREV_YEAR"] = "前年（押し続けるとメニュー表示）";
Calendar._TT["PREV_MONTH"] = "前月（押し続けるとメニュー表示）";
Calendar._TT["GO_TODAY"] = "今日へ移動";
Calendar._TT["NEXT_MONTH"] = "翌月（押し続けるとメニュー表示）";
Calendar._TT["NEXT_YEAR"] = "翌年（押し続けるとメニュー表示）";
Calendar._TT["SEL_DATE"] = "日付を選択";
Calendar._TT["DRAG_TO_MOVE"] = "ドラッグで移動";
Calendar._TT["PART_TODAY"] = "（今日）";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "%s を先頭に表示";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "0,6";

Calendar._TT["CLOSE"] = "閉じる";
Calendar._TT["TODAY"] = "今日";
Calendar._TT["TIME_PART"] = "クリックまたはドラッグで値を変更（Shift クリックで減少）";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "%Y-%m-%d";
Calendar._TT["TT_DATE_FORMAT"] = "%m月%e日（%a）";

Calendar._TT["WK"] = "週";
Calendar._TT["TIME"] = "時刻:";
