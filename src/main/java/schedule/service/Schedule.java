package schedule.service;


import schedule.dao.Jdbc;

/**
 * @author white matter
 */
public class Schedule extends Thread {
    private int start;
    private int end;

    public Schedule(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public void run() {
        Message message = new Message();
        Student student = new Student();
        Relation relation = new Relation();
        for (int student_id = start; student_id < end; student_id++) {
            String studentA = student.insertStudent(String.valueOf(student_id));
            String messageA = message.oneSchedule(String.valueOf(student_id));
            String relationA = relation.insertRelation(String.valueOf(student_id));

            if (!studentA.equals("学生表")) {
                System.out.println(studentA + student_id);
            }

            if (!messageA.equals("课表")) {
                System.out.println(messageA + student_id);
            }

            if (!relationA.equals("选课关系表")) {
                System.out.println(relationA + student_id);
            }
        }

        //使用 左联查 同时删除两张表中的 选修
        new Jdbc().deleteTwo();
    }

    public static void main(String[] args) {   //以 651 X 8 =5208 共 8个线程
        new Schedule(2018210000, 2018210650).start();
        new Schedule(2018210651, 2018211301).start();
        new Schedule(2018211302, 2018211952).start();
        new Schedule(2018211953, 2018212603).start();
        new Schedule(2018212604, 2018213254).start();
        new Schedule(2018213255, 2018213905).start();
        new Schedule(2018213906, 2018214556).start();
        new Schedule(2018214557, 2018215207).start();
    }
}
