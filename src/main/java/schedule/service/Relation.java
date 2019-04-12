package schedule.service;

import schedule.dao.Jdbc;
import sun.java2d.pipe.AAShapePipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author white matter
 */
public class Relation {
    public String insertRelation(String student_id) {
        Jdbc jdbc = new Jdbc();
        String all = SendGet.sendGet(student_id);

        String studentR1 = "(?<=(<li>〉〉2018-2019学年2学期学生课表>>)).*(?=(</li></ul></div>))";
        String studentR2 = "\\d*";
        //设置名字长度
        String studentR3 = "[\\u4e00-\\u9fa5]{2,15}";

        Matcher matcherR1 = Pattern.compile(studentR1).matcher(all);
        List<String> list = new ArrayList<>();

        while (matcherR1.find()) {
            Matcher matcherR2 = Pattern.compile(studentR2).matcher(matcherR1.group());
            Matcher matcherR3 = Pattern.compile(studentR3).matcher(matcherR1.group());
            if (matcherR2.find()) {
                list.add(matcherR2.group());
                if (matcherR3.find())
                    list.add(matcherR3.group());
            }
        }

        String relationR = "(?<=(rowspan='\\d'>))[^<]{1,30}(?=(</td))";
        Matcher matcherRelationR = Pattern.compile(relationR).matcher(all);

        while (matcherRelationR.find()) {
            list.add(matcherRelationR.group());
        }

        int result = jdbc.insertRelation(list);
        if (result == 0) {
            return "选课关系表学号不存在!";
        }
        if (result == 2) {
            return "---选课关系表学号存 但 插入失败";
        } else {
            return "选课关系表";
        }
    }

    public static void main(String[] args) {

        List<Integer> list = new ArrayList<>();
        list.add(5);
        list.add(1);
        list.add(2);
        list.add(3);
        Collections.sort(list);
        for (Integer integer : list) {
            System.out.println(integer);
            ;
        }
    }
}
