package org.akaza.openclinica.bean.managestudy;

import org.akaza.openclinica.bean.core.EntityBean;

import java.util.ArrayList;
import java.util.Date;

public class ProtocolDeviationBean extends EntityBean {
    private static final long serialVersionUID = -8498550403753118474L;
    private int protocolDeviationId;
    private String label;
    private int studyId;
    /*private int severityId;
    private String severityLabel;
    private String description;*/
    private short itemA1;
    private short itemA2;
    private Date itemA3;
    private Date itemA4;
    private Date itemA5;
    private short itemA6;
    private short itemA7;
    private Date itemA7_1;
    private short itemA8;
    private short itemB1;
    private short itemB2;
    private short itemB3;
    private short itemB4;
    private short itemB5;
    private short itemB6;
    private short itemB7;
    private short itemB8;
    private short itemB9;
    private short itemB10;
    private short itemB11;
    private short itemB12;
    private short itemB13;
    private short itemB14;
    private short itemB15;
    private short itemB16;
    private short itemB17;
    private short itemB18;
    private short itemC1_1;
    private short itemC1_2;
    private short itemC1_3;
    private short itemC1_4;
    private short itemC1_5;
    private short itemC1_6;
    private short itemC1_7;
    private short itemC1_8;
    private short itemC1_9;
    private String itemC1_10;
    private String itemC2;
    private Date itemD1_A;
    private String itemD1_B;
    private String itemE1;
    private String itemE2;
    private String itemE3;
    private String itemE4;
    private String itemF1;
    private String itemF2;
    private Date itemF3;
    private short itemG1;
    private short itemG2_1;
    private short itemG2_2;
    private short itemG2_3;
    private short itemG2_4;
    private short itemG3;
    private short itemG4;
    private short itemG5;
    private Date itemG6;
    private String itemG6_1_A;
    private short itemG6_1_B;
    private String itemG6_1_C;
    private String itemG6_2_A;
    private short itemG6_2_B;
    private String itemG6_2_C;
    private String itemG6_3_A;
    private short itemG6_3_B;
    private String itemG6_3_C;
    private String itemG6_4_A;
    private short itemG6_4_B;
    private String itemG6_4_C;
    private String itemG7;
    private String itemG8;
    private String itemG9;


    private ArrayList<ProtocolDeviationSubjectBean> subjects = new ArrayList<>();

    public int getProtocolDeviationId() {
        return protocolDeviationId;
    }

    public void setProtocolDeviationId(int protocolDeviationId) {
        this.protocolDeviationId = protocolDeviationId;
    }

    public int getStudyId() {
        return studyId;
    }

    public void setStudyId(int studyId) {
        this.studyId = studyId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public short getItemA1() {
        return itemA1;
    }

    public void setItemA1(short itemA1) {
        this.itemA1 = itemA1;
    }

    public short getItemA2() {
        return itemA2;
    }

    public void setItemA2(short itemA2) {
        this.itemA2 = itemA2;
    }

    public Date getItemA3() {
        return itemA3;
    }

    public void setItemA3(Date itemA3) {
        this.itemA3 = itemA3;
    }

    public Date getItemA4() {
        return itemA4;
    }

    public void setItemA4(Date itemA4) {
        this.itemA4 = itemA4;
    }

    public Date getItemA5() {
        return itemA5;
    }

    public void setItemA5(Date itemA5) {
        this.itemA5 = itemA5;
    }

    public short getItemA6() {
        return itemA6;
    }

    public void setItemA6(short itemA6) {
        this.itemA6 = itemA6;
    }

    public short getItemA7() {
        return itemA7;
    }

    public void setItemA7(short itemA7) {
        this.itemA7 = itemA7;
    }

    public Date getItemA7_1() {
        return itemA7_1;
    }

    public void setItemA7_1(Date itemA7_1) {
        this.itemA7_1 = itemA7_1;
    }

    public short getItemA8() {
        return itemA8;
    }

    public void setItemA8(short itemA8) {
        this.itemA8 = itemA8;
    }

    public short getItemB1() {
        return itemB1;
    }

    public void setItemB1(short itemB1) {
        this.itemB1 = itemB1;
    }

    public short getItemB2() {
        return itemB2;
    }

    public void setItemB2(short itemB2) {
        this.itemB2 = itemB2;
    }

    public short getItemB3() {
        return itemB3;
    }

    public void setItemB3(short itemB3) {
        this.itemB3 = itemB3;
    }

    public short getItemB4() {
        return itemB4;
    }

    public void setItemB4(short itemB4) {
        this.itemB4 = itemB4;
    }

    public short getItemB5() {
        return itemB5;
    }

    public void setItemB5(short itemB5) {
        this.itemB5 = itemB5;
    }

    public short getItemB6() {
        return itemB6;
    }

    public void setItemB6(short itemB6) {
        this.itemB6 = itemB6;
    }

    public short getItemB7() {
        return itemB7;
    }

    public void setItemB7(short itemB7) {
        this.itemB7 = itemB7;
    }

    public short getItemB8() {
        return itemB8;
    }

    public void setItemB8(short itemB8) {
        this.itemB8 = itemB8;
    }

    public short getItemB9() {
        return itemB9;
    }

    public void setItemB9(short itemB9) {
        this.itemB9 = itemB9;
    }

    public short getItemB10() {
        return itemB10;
    }

    public void setItemB10(short itemB10) {
        this.itemB10 = itemB10;
    }

    public short getItemB11() {
        return itemB11;
    }

    public void setItemB11(short itemB11) {
        this.itemB11 = itemB11;
    }

    public short getItemB12() {
        return itemB12;
    }

    public void setItemB12(short itemB12) {
        this.itemB12 = itemB12;
    }

    public short getItemB13() {
        return itemB13;
    }

    public void setItemB13(short itemB13) {
        this.itemB13 = itemB13;
    }

    public short getItemB14() {
        return itemB14;
    }

    public void setItemB14(short itemB14) {
        this.itemB14 = itemB14;
    }

    public short getItemB15() {
        return itemB15;
    }

    public void setItemB15(short itemB15) {
        this.itemB15 = itemB15;
    }

    public short getItemB16() {
        return itemB16;
    }

    public void setItemB16(short itemB16) {
        this.itemB16 = itemB16;
    }

    public short getItemB17() {
        return itemB17;
    }

    public void setItemB17(short itemB17) {
        this.itemB17 = itemB17;
    }

    public short getItemB18() {
        return itemB18;
    }

    public void setItemB18(short itemB18) {
        this.itemB18 = itemB18;
    }

    public short getItemC1_1() {
        return itemC1_1;
    }

    public void setItemC1_1(short itemC1_1) {
        this.itemC1_1 = itemC1_1;
    }

    public short getItemC1_2() {
        return itemC1_2;
    }

    public void setItemC1_2(short itemC1_2) {
        this.itemC1_2 = itemC1_2;
    }

    public short getItemC1_3() {
        return itemC1_3;
    }

    public void setItemC1_3(short itemC1_3) {
        this.itemC1_3 = itemC1_3;
    }

    public short getItemC1_4() {
        return itemC1_4;
    }

    public void setItemC1_4(short itemC1_4) {
        this.itemC1_4 = itemC1_4;
    }

    public short getItemC1_5() {
        return itemC1_5;
    }

    public void setItemC1_5(short itemC1_5) {
        this.itemC1_5 = itemC1_5;
    }

    public short getItemC1_6() {
        return itemC1_6;
    }

    public void setItemC1_6(short itemC1_6) {
        this.itemC1_6 = itemC1_6;
    }

    public short getItemC1_7() {
        return itemC1_7;
    }

    public void setItemC1_7(short itemC1_7) {
        this.itemC1_7 = itemC1_7;
    }

    public short getItemC1_8() {
        return itemC1_8;
    }

    public void setItemC1_8(short itemC1_8) {
        this.itemC1_8 = itemC1_8;
    }

    public short getItemC1_9() {
        return itemC1_9;
    }

    public void setItemC1_9(short itemC1_9) {
        this.itemC1_9 = itemC1_9;
    }

    public String getItemC1_10() {
        return itemC1_10;
    }

    public void setItemC1_10(String itemC1_10) {
        this.itemC1_10 = itemC1_10;
    }

    public String getItemC2() {
        return itemC2;
    }

    public void setItemC2(String itemC2) {
        this.itemC2 = itemC2;
    }

    public Date getItemD1_A() {
        return itemD1_A;
    }

    public void setItemD1_A(Date itemD1_A) {
        this.itemD1_A = itemD1_A;
    }

    public String getItemD1_B() {
        return itemD1_B;
    }

    public void setItemD1_B(String itemD1_B) {
        this.itemD1_B = itemD1_B;
    }

    public String getItemE1() {
        return itemE1;
    }

    public void setItemE1(String itemE1) {
        this.itemE1 = itemE1;
    }

    public String getItemE2() {
        return itemE2;
    }

    public void setItemE2(String itemE2) {
        this.itemE2 = itemE2;
    }

    public String getItemE3() {
        return itemE3;
    }

    public void setItemE3(String itemE3) {
        this.itemE3 = itemE3;
    }

    public String getItemE4() {
        return itemE4;
    }

    public void setItemE4(String itemE4) {
        this.itemE4 = itemE4;
    }

    public String getItemF1() {
        return itemF1;
    }

    public void setItemF1(String itemF1) {
        this.itemF1 = itemF1;
    }

    public String getItemF2() {
        return itemF2;
    }

    public void setItemF2(String itemF2) {
        this.itemF2 = itemF2;
    }

    public Date getItemF3() {
        return itemF3;
    }

    public void setItemF3(Date itemF3) {
        this.itemF3 = itemF3;
    }

    public short getItemG1() {
        return itemG1;
    }

    public void setItemG1(short itemG1) {
        this.itemG1 = itemG1;
    }

    public short getItemG2_1() {
        return itemG2_1;
    }

    public void setItemG2_1(short itemG2_1) {
        this.itemG2_1 = itemG2_1;
    }

    public short getItemG2_2() {
        return itemG2_2;
    }

    public void setItemG2_2(short itemG2_2) {
        this.itemG2_2 = itemG2_2;
    }

    public short getItemG2_3() {
        return itemG2_3;
    }

    public void setItemG2_3(short itemG2_3) {
        this.itemG2_3 = itemG2_3;
    }

    public short getItemG2_4() {
        return itemG2_4;
    }

    public void setItemG2_4(short itemG2_4) {
        this.itemG2_4 = itemG2_4;
    }

    public short getItemG3() {
        return itemG3;
    }

    public void setItemG3(short itemG3) {
        this.itemG3 = itemG3;
    }

    public short getItemG4() {
        return itemG4;
    }

    public void setItemG4(short itemG4) {
        this.itemG4 = itemG4;
    }

    public short getItemG5() {
        return itemG5;
    }

    public void setItemG5(short itemG5) {
        this.itemG5 = itemG5;
    }

    public Date getItemG6() {
        return itemG6;
    }

    public void setItemG6(Date itemG6) {
        this.itemG6 = itemG6;
    }

    public String getItemG6_1_A() {
        return itemG6_1_A;
    }

    public void setItemG6_1_A(String itemG6_1_A) {
        this.itemG6_1_A = itemG6_1_A;
    }

    public short getItemG6_1_B() {
        return itemG6_1_B;
    }

    public void setItemG6_1_B(short itemG6_1_B) {
        this.itemG6_1_B = itemG6_1_B;
    }

    public String getItemG6_1_C() {
        return itemG6_1_C;
    }

    public void setItemG6_1_C(String itemG6_1_C) {
        this.itemG6_1_C = itemG6_1_C;
    }

    public String getItemG6_2_A() {
        return itemG6_2_A;
    }

    public void setItemG6_2_A(String itemG6_2_A) {
        this.itemG6_2_A = itemG6_2_A;
    }

    public short getItemG6_2_B() {
        return itemG6_2_B;
    }

    public void setItemG6_2_B(short itemG6_2_B) {
        this.itemG6_2_B = itemG6_2_B;
    }

    public String getItemG6_2_C() {
        return itemG6_2_C;
    }

    public void setItemG6_2_C(String itemG6_2_C) {
        this.itemG6_2_C = itemG6_2_C;
    }

    public String getItemG6_3_A() {
        return itemG6_3_A;
    }

    public void setItemG6_3_A(String itemG6_3_A) {
        this.itemG6_3_A = itemG6_3_A;
    }

    public short getItemG6_3_B() {
        return itemG6_3_B;
    }

    public void setItemG6_3_B(short itemG6_3_B) {
        this.itemG6_3_B = itemG6_3_B;
    }

    public String getItemG6_3_C() {
        return itemG6_3_C;
    }

    public void setItemG6_3_C(String itemG6_3_C) {
        this.itemG6_3_C = itemG6_3_C;
    }

    public String getItemG6_4_A() {
        return itemG6_4_A;
    }

    public void setItemG6_4_A(String itemG6_4_A) {
        this.itemG6_4_A = itemG6_4_A;
    }

    public short getItemG6_4_B() {
        return itemG6_4_B;
    }

    public void setItemG6_4_B(short itemG6_4_B) {
        this.itemG6_4_B = itemG6_4_B;
    }

    public String getItemG6_4_C() {
        return itemG6_4_C;
    }

    public void setItemG6_4_C(String itemG6_4_C) {
        this.itemG6_4_C = itemG6_4_C;
    }

    public String getItemG7() {
        return itemG7;
    }

    public void setItemG7(String itemG7) {
        this.itemG7 = itemG7;
    }

    public String getItemG8() {
        return itemG8;
    }

    public void setItemG8(String itemG8) {
        this.itemG8 = itemG8;
    }

    public String getItemG9() {
        return itemG9;
    }

    public void setItemG9(String itemG9) {
        this.itemG9 = itemG9;
    }

    public ArrayList<ProtocolDeviationSubjectBean> getSubjects() {
        return subjects;
    }

    public void setSubjects(ArrayList<ProtocolDeviationSubjectBean> subjects) {
        this.subjects = subjects;
    }
}
