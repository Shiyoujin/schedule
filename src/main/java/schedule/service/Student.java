package schedule.service;


import schedule.dao.Jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author white matter
 */
public class Student {
    public String insertStudent(String student_id) {
        Jdbc jdbc = new Jdbc();
        String all = SendGet.sendGet(student_id);

        String studentR1 = "(?<=(<li>〉〉2018-2019学年2学期学生课表>>)).*(?=(</li></ul></div>))";
        String studentR2 = "\\d*";
        //设置名字长度
        String studentR3 = "[\\u4e00-\\u9fa5]{2,15}";

        Matcher matcherR1 = Pattern.compile(studentR1).matcher(all);
        List<String> s_List = new ArrayList<>();

        while (matcherR1.find()) {
            Matcher matcherR2 = Pattern.compile(studentR2).matcher(matcherR1.group());
            Matcher matcherR3 = Pattern.compile(studentR3).matcher(matcherR1.group());
            if (matcherR2.find()) {
                s_List.add(matcherR2.group());
                if (matcherR3.find())
                    s_List.add(matcherR3.group());
            }
        }

        int result = jdbc.insertStudent(s_List);
        if (result == 0) {
            return "学生表学号不存在!";
        }
        if (result == 2) {
            return "---学生表学号存在 但 插入失败---疑似退学";
        } else {
            return "学生表";
        }
    }

    public static void main(String[] args) {
        new Student().insertStudent("2018210000");
    }
}

