package schedule.dao;

import schedule.bean.ScheduleBean;
import schedule.service.Search;

import java.sql.*;
import java.util.*;


/**
 * @author white matter
 */
public class Jdbc {
    /**
     * @return Connection
     * @Description 创建 数据库的连接
     * @author white matter
     */
    private static Connection getConn() {

        Connection conn = null;
        try {
            //加载 JDBC 的驱动
            String driverName = "com.mysql.cj.jdbc.Driver";
            //classLoader,加载对应驱动
            Class.forName(driverName);

            //创建数据库的链接, student 为库名，其后为数据库设置了  UTF-8和 时区，不然 中文会乱码
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/schedule?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT%2B8", "root", "");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * @return int 0 1 2 3 分别代表不同的 方法执行后 成功与否的 因素
     * @Description插入 student表
     * @author white matter
     * @Param List<String>
     */
    public int insertStudent(List<String> list) {
        String sql = "INSERT INTO student(student_id,student_name) VALUES(?,?)";
        Connection connection = getConn();
        PreparedStatement pstmt = null;
        int i = 0;
        if (list.size() != 2) {
            //学生学号 不存在
            return 0;
        } else {
            try {
                pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, list.get(0));
                pstmt.setString(2, list.get(1));
                i = pstmt.executeUpdate();
                pstmt.close();
                connection.close();
                if (i > 0)
                //学号存在 且 插入成功
                {
                    return 1;
                } else
                //学号存在 但 插入失败
                {
                    return 2;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                //SQL错误
                return 3;
            }
        }
    }


    /**
     * @return int
     * @Description 插入 schedule 课表详细信息
     * @Param List<String>
     * @author white matter
     */
    public int insert_Nine(List<String> list) {
        int i = 0;
        if (list.size() < 14) {
            return 0;
        } else {
            for (; i < list.size(); i += 9) {
                String sql = "INSERT INTO schedule(student_id,student_name,classify,s_message,s_class,s_classify,s_teacher,s_time,s_where) VALUE(?,?,?,?,?,?,?,?,?)";
                PreparedStatement pstmt = null;
                Connection connection = getConn();
                try {
                    pstmt = connection.prepareStatement(sql);
                    pstmt.setString(1, list.get(i));
                    pstmt.setString(2, list.get(i + 1));
                    pstmt.setString(3, list.get(i + 2));
                    pstmt.setString(4, list.get(i + 3));
                    pstmt.setString(5, list.get(i + 4));
                    pstmt.setString(6, list.get(i + 5));
                    pstmt.setString(7, list.get(i + 6));
                    pstmt.setString(8, list.get(i + 7));
                    pstmt.setString(9, list.get(i + 8));
                    pstmt.executeUpdate();
                    pstmt.close();
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        //因为大一的学生，课表的课 肯定多多于三节！
        if (i > 9) {
            return 1;
        } else {
            return 2;
        }
    }

    /**
     * @return int
     * @Description 插入 relation表 选课关系表
     * @Param List<String>
     * @author white matter
     */
    public int insertRelation(List<String> list) {
        int i = 2, a = 0;
        if (list.size() < 2) {
            return 0;
        } else {
            for (; i < list.size(); i += 3) {
                String sql = "INSERT INTO relation(student_id,student_name,s_message,s_class,s_classify) VALUE (?,?,?,?,?)";
                PreparedStatement pstmt = null;
                Connection connection = getConn();
                try {
                    pstmt = connection.prepareStatement(sql);
                    pstmt.setString(1, list.get(0));
                    pstmt.setString(2, list.get(1));
                    pstmt.setString(3, list.get(i));
                    pstmt.setString(4, list.get(i + 1));
                    pstmt.setString(5, list.get(i + 2));
                    a = pstmt.executeUpdate();
                    pstmt.close();
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (a > 0) {
                return 1;
            } else {
                return 2;
            }
        }
    }

    /**
     * @return int
     * @Description 左联查，同时删除 schedule 和 relation 两表中的选修
     * @Param List<String>
     * @author white matter
     */
    public boolean deleteTwo() {
        //必须要 schedule，relation 两张表 同时有选修才会生效， 仅一张表有，并不会生效
        String sql = "DELETE schedule,relation FROM schedule LEFT JOIN relation ON schedule.s_classify = relation.s_classify WHERE schedule.s_classify = ?";
        Connection connection = Jdbc.getConn();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, "选修");
            int result = pstmt.executeUpdate();
            pstmt.close();
            connection.close();
            if (result != 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    //搜索条件的 列上 加索引
    public HashMap<String, List<String>> findSchedule(String content) {
        String sql = "SELECT student_id,student_name,s_class FROM relation WHERE student_id = ? OR student_name = ?";
        Connection connection = Jdbc.getConn();
        PreparedStatement pstmt = null;
        ResultSet res = null;
        List<String> listA = new ArrayList<>();
        List<String> listB = new ArrayList<>();
        HashMap<String, List<String>> hashMap = new HashMap<>(1000);
        int i = 0;
        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, content);
            pstmt.setString(2, content);
            res = pstmt.executeQuery();
            while (res.next()) {
                if (i < 1) {
                    listA.add(res.getString("student_id"));
                    listA.add(res.getString("student_name"));
                    i++;
                }
                listB.add(res.getString("s_class"));
            }
            hashMap.put("student", listA);
            hashMap.put("schedule", listB);
            res.close();
            connection.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hashMap;
    }

    public HashMap<String, List> findDetail(HashMap<String, List<String>> hashMap) {

        List<String> listB = hashMap.get("schedule");

        HashMap<String, List> resultMap = new HashMap<>(1000);
        List s_list = new ArrayList();
        s_list = hashMap.get("student");
        String student_id = String.valueOf(s_list.get(0));
        resultMap.put("student", s_list);

        List<ScheduleBean> listBean = new ArrayList<>();

        for (String string : listB) {
            try {
                String sql = "SELECT s_message,s_teacher,s_time,s_where FROM schedule WHERE s_class = ? AND student_id = ?";
                Connection connection = Jdbc.getConn();
                PreparedStatement pstmt = null;
                ResultSet res = null;

                pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, string);
                pstmt.setString(2, student_id);
                res = pstmt.executeQuery();
                while (res.next()) {
                    ScheduleBean scheduleBean = new ScheduleBean();
                    scheduleBean.setS_message(res.getString("s_message"));
                    scheduleBean.setS_teacher(res.getString("s_teacher"));
                    scheduleBean.setS_time(res.getString("s_time"));
                    scheduleBean.setS_where(res.getString("s_where"));
                    listBean.add(scheduleBean);
                }
                resultMap.put("schedule", listBean);
                res.close();
                pstmt.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return resultMap;
    }

    public static void main(String[] args) {
        Jdbc jdbc = new Jdbc();
        Search search = new Search();
        HashMap<String, List<String>> listHashMap = jdbc.findSchedule("2018211314");
        HashMap<String, List> hashMap = jdbc.findDetail(listHashMap);
        System.out.println(hashMap.get("student").get(0));
        System.out.println(hashMap.get("student").get(1));
//        List<ScheduleBean> list = hashMap.get("schedule");
//        for (ScheduleBean scheduleBean : list){
//            System.out.println(scheduleBean.getS_message());
//            System.out.println(scheduleBean.getS_teacher());
//            System.out.println(scheduleBean.getS_where());
//            System.out.println(scheduleBean.getS_time());
//        }
//        jdbc.deleteTwo();
        System.out.println(search.createFindS(hashMap));
    }

}
