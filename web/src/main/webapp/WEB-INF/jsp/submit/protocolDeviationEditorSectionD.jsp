<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div class="section-d" style="display: flex; flex-direction: column">
    <h2>D. Reporting Incident</h2>
    <ol>
        <li>
            <span class="question">Local IRB/ethics board reporting: Date when site staff reported incident to local
            IRB/ethics board:</span>
            <div class="detail">
                <div>
                    a. <input type="text" name="item_d_1_a" id="item_d_1_a">
                    <a href="#">
                        <img src="images/bt_Calendar.gif" title="" border="0" id="item_d_1_a-trigger" />
                    </a>
                    <script type="text/javascript">
                        Calendar.setup({inputField  : "item_d_1_a", ifFormat: "%d-%b-%Y",
                            button: "item_d_1_a-trigger" });
                    </script>
                </div>
                <div>b. If the site staff did not report to local IRB, provide rationale for not reporting:</div>
                <textarea name="item_d_1_b"></textarea>
            </div>

        </li>
    </ol>

</div>