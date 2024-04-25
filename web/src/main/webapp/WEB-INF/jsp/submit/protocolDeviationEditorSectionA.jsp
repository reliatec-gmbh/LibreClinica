<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div class="section-a" style="display: flex; flex-direction: column">
    <h2>A. Incident description</h2>
    <ol>
        <li>
            <span class="question">
                Is this incident associated with an adverse event or the development of TB evaluation?
            </span>
            <div class="detail">
                <label><input type="radio" name="item_a_1" value="1">Yes</label>
                <label><input type="radio" name="item_a_1" value="0">No</label>
            </div>
        </li>
        <li>
            <span class="question">
                Did this incident occurred because of modifications due to COVID-19 or a COVID-19 diagnosis?
            </span>
            <div class="detail">
                <label><input type="radio" name="item_a_2" value="1">Yes</label>
                <label><input type="radio" name="item_a_2" value="0">No</label>
            </div>
        </li>
        <li>
            <span class="question">
                Date the incident occurred:
                <div class="detail">
                    <input type="text" name="item_a_3" id="item_a_3"/>
                    <a href="#">
                        <img src="images/bt_Calendar.gif" title="" border="0" id="item_a_3-trigger" />
                        <script type="text/javascript">
                            Calendar.setup({inputField  : "item_a_3", ifFormat: "%d-%b-%Y",
                                button: "item_a_3-trigger" });
                        </script>
                    </a>
                </div>
            </span>
        </li>
        <li>
            <span class="question">
                Date site staff became aware of the incident:
                <div class="detail">
                    <input type="text" name="item_a_4" id="item_a_4"/>
                    <a href="#">
                        <img src="images/bt_Calendar.gif" title="" border="0" id="item_a_4-trigger" />
                        <script type="text/javascript">
                            Calendar.setup({inputField  : "item_a_4", ifFormat: "%d-%b-%Y",
                                button: "item_a_4-trigger" });
                        </script>
                    </a>
                </div>
            </span>
        </li>
        <li>
            <span class="question">
                Date of first communication with the clinical Research Branch about the incident:
                <div class="detail">
                    <input type="text" name="item_a_5" id="item_a_5"/>
                    <a href="#">
                        <img src="images/bt_Calendar.gif" title="" border="0" id="item_a_5-trigger" />
                        <script type="text/javascript">
                            Calendar.setup({inputField  : "item_a_5", ifFormat: "%d-%b-%Y",
                                button: "item_a_5-trigger" });
                        </script>
                    </a>
                </div>
            </span>
        </li>
        <li>
            <span class="question">
                As a result of the incident describe above, the participant:
            </span>
            <div class="detail vertical">
                <label><input type="radio" name="item_a_6" value="1">
                    Remained in study</label>

                <label><input type="radio" name="item_a_6" value="2">
                Discontinued study treatment, was referred to the local program for care, and continued study
                follow-up (Complete Study Treatment Completion Form)</label>

                <label><input type="radio" name="item_a_6" value="3">
                Discontinued study treatment, was referred to the local program for care, and permanently discontinued
                from study (Complete both Study Treatment and Follow-Up Completion Forms)</label>

                <label><input type="radio" name="item_a_6" value="4">
                Discontinued study follow-up and permanently discontinued
                from study (Complete Follow-Up Completion Form)</label>

                <label><input type="radio" name="item_a_6" value="5">
                    Died <strong>(Complete Notification of Death Form)</strong></label>

                <label><input type="radio" name="item_a_6" value="6"> Other</label>

            </div>
        </li>
        <li>
            <span class="question">Did site report incident to the participant:</span>


            <div class="detail">
                <label><input type="radio" name="item_a_7" value="1">Yes</label>
                <label><input type="radio" name="item_a_7" value="0">No</label>
                <div>
                    a. Date when site staff reported incident to the participant
                    <div>
                        <input type="text" id="item_a_7_1" name="item_a_7_1"/>
                        <a href="#">
                            <img src="images/bt_Calendar.gif" title="" border="0" id="item_a_7_1-trigger" />
                            <script type="text/javascript">
                                Calendar.setup({inputField  : "item_a_7_1", ifFormat: "%d-%b-%Y",
                                    button: "item_a_7_1-trigger" });
                            </script>
                        </a>
                    </div>
                </div>
            </div>
        </li>
        <li>
            <span class="question">Did this incident occur among more than one participant?</span>
            <div class="detail">
                <label><input type="radio" name="item_a_8" value="1">Yes</label>
                <label><input type="radio" name="item_a_8" value="0">No</label>
            </div>
        </li>
    </ol>

</div>