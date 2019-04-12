package schedule.service;


import schedule.dao.Jdbc;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author white matter
 */

//测试 见 web项目， schedule类
public class Message {
    //定义三个静态变量
    static StringBuffer sbTime = new StringBuffer();
    static StringBuffer sbWhere = new StringBuffer();
    static String teacher = null;

    /**
     * @return int 0 1 2 3 分别代表不同的 方法执行后 成功与否的 因素
     * @Description 爬取 某学号的 课表
     * @author white matter
     * @Param List<String>
     */
    public String oneSchedule(String student_id) {
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

        //爬 教学班分类，比如 理论和实验实践这两种
        String classifyR = "(?<=(align='center'>))理论(?=(</td>))|(?<=(align='center'>))实验实践(?=(</td))";
        Matcher matcherClassifyR = Pattern.compile(classifyR).matcher(all);
        List<String> AllList = new ArrayList();
        //爬 课程号-课程名，教学班，类别  三个列
        //屏蔽掉 标签中 含 < 的
        String massageR = "(?<=(rowspan='\\d'>))[^<]{1,30}(?=(</td))";
        Matcher matcherMessageR = Pattern.compile(massageR).matcher(all);
        //爬 教师，上课时间，地点  三个列
        // ^ 中 含 某一个 都会被 屏蔽， 比如 ^春 冯春宝 则会被 屏蔽
        String teacherR1 = "(?<=(<td>))[^&\\|\\^<]{2,35}(?=(</td>))";
        Matcher matcherTeacherR1 = Pattern.compile(teacherR1).matcher(all);
        // ^.*$ 表示 匹配整个 句子 为何不能用 .*  ？？  正向否定预查
        String teacherR2 = "(?!.*教学|课程|类别|选课|教师|上课|地点|学生|备注|停课|.*时间|序号|类型|学期|补课|代课)^.*$";
        //记录 每一大行的 包含多少小行
        String rowNumberR = "(?<=(<tdrowspan='))\\d(?=('align='center'>正常</td>))";
        Matcher matcherRowNumber = Pattern.compile(rowNumberR).matcher(all);
        List<Integer> numberList = new ArrayList<>();
        // 每一大行 所包含的 小行行数的 numberList
        while (matcherRowNumber.find()) {
            numberList.add(Integer.valueOf(matcherRowNumber.group()));
        }

        //按爬的 顺序装进 Tlist 里面
        List Tlist = new ArrayList();
        //将 Tlist 按 教师，上课时间，地点 处理后装进 threeList 里面
        List threeList = new ArrayList();
        //使用两个正则匹配，过滤
        while (matcherTeacherR1.find()) {
            Matcher matcherTeacherR2 = Pattern.compile(teacherR2).matcher(matcherTeacherR1.group());
            while (matcherTeacherR2.find()) {
                //这里面的 Tlist 可能包含 补课的课表信息
                Tlist.add(matcherTeacherR2.group());
            }
        }

        List<String> classifyList = new ArrayList<>();
        List<String> messageList = new ArrayList<>();
        List<String> teacherList = new ArrayList<>();

        int j = 0;
        int p = 0;
        while (matcherClassifyR.find()) {
            int integer = numberList.get(p);
            for (int i = 0; i < integer; i++) {
                classifyList.add(matcherClassifyR.group());
            }
            p++;
        }
        p = 0;

        int u = 0;
        while (matcherMessageR.find()) {
            u++;
            int integer = numberList.get(p);
            for (int i = 0; i < integer; i++) {
                messageList.add(matcherMessageR.group());
            }
            if (u % 3 == 0) {
                p++;
            }
        }
        for (Integer integer : numberList) {
            for (int i = 0; i < integer; i++) {
                teacherList.add(String.valueOf(Tlist.get(j)));
                teacherList.add(String.valueOf(Tlist.get(j + 1)));
                teacherList.add(String.valueOf(Tlist.get(j + 2)));
                j += 3;
            }
        }

        List<String> newMessageList = new ArrayList<>();
        int r = 0;
        for (Integer integer : numberList) {
            for (int i = 0; i < integer; i++) {
                newMessageList.add(messageList.get(r));
                newMessageList.add(messageList.get(r + integer));
                newMessageList.add(messageList.get(r + 2 * integer));
            }
            r += integer * 3;
        }

        int k = 0;
        int w = 0;
        for (String string : classifyList) {
            AllList.add(s_List.get(0));
            AllList.add(s_List.get(1));
            AllList.add(string);
            AllList.add(newMessageList.get(k));
            AllList.add(newMessageList.get(k + 1));
            AllList.add(newMessageList.get(k + 2));
            if (k < newMessageList.size() - 3) {
                k += 3;
            }

            AllList.add(teacherList.get(w));
            AllList.add(teacherList.get(w + 1));
            AllList.add(teacherList.get(w + 2));
            if (w < teacherList.size() - 3) {
                w += 3;
            }
        }

        int result = jdbc.insert_Nine(AllList);
        if (result == 0) {
            return "课表学号不存在!";
        }
        if (2 == result) {
            return "---课表学号存在插入失败";
        } else {
            return "课表";
        }
    }

}





