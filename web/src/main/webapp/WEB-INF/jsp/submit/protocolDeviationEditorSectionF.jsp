<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div class="section-f" style="display: flex; flex-direction: column">
    <h2>F. Form Completion</h2>
    <ol>
        <li>
            <span class="question">Name of Principal Investigator or Co-PI:</span>
            <div class="detail">
                <input type="text" name="item_f_1" id="item_f_1">
            </div>
        </li>
        <li>
            <span class="question">Name of person completing form:</span>
            <div class="detail">
                <input type="text" name="item_f_2">
            </div>
        </li>
        <li>
            <span class="question">Date Form Completed:</span>
            <div class="detail">
                <input type="text" name="item_f_3" id="item_f_3">
                <a href="#">
                    <img src="images/bt_Calendar.gif" title="" border="0" id="item_f_3-trigger" />
                </a>
                <script type="text/javascript">
                    Calendar.setup({inputField  : "item_f_3", ifFormat: "%d-%b-%Y",
                        button: "item_f_3-trigger" });
                </script>
            </div>
        </li>
    </ol>
    
</div>