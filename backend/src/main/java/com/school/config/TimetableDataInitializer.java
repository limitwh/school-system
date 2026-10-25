package com.school.config;

import com.school.entity.Grade;
import com.school.entity.GradeSubject;
import com.school.entity.Period;
import com.school.entity.Subject;
import com.school.repository.GradeRepository;
import com.school.repository.GradeSubjectRepository;
import com.school.repository.PeriodRepository;
import com.school.repository.SubjectRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课程表种子数据：科目、节次作息、各年级周课时计划。
 * 幂等：已存在的科目/作息/年级计划不会重复插入。
 */
@Component
public class TimetableDataInitializer implements CommandLineRunner {

    /** 名称、编码、类别、排序 */
    private static final String[][] SUBJECTS = {
            {"语文", "CHINESE", "ACADEMIC", "1"},
            {"数学", "MATH", "ACADEMIC", "2"},
            {"英语", "ENGLISH", "ACADEMIC", "3"},
            {"体育", "PE", "ACADEMIC", "4"},
            {"美术", "ART", "ACADEMIC", "5"},
            {"科学", "SCIENCE", "ACADEMIC", "6"},
            {"道德与法律", "MORAL", "ACADEMIC", "7"},
            {"政治", "POLITICS", "ACADEMIC", "8"},
            {"生物", "BIOLOGY", "ACADEMIC", "9"},
            {"物理", "PHYSICS", "ACADEMIC", "10"},
            {"化学", "CHEMISTRY", "ACADEMIC", "11"},
            {"音乐", "MUSIC", "ACTIVITY", "12"},
            {"班会", "CLASS_MEETING", "ACTIVITY", "13"},
            {"自习", "SELF_STUDY", "ACTIVITY", "14"}
    };

    private static final String[][] PRIMARY_PERIODS = {
            {"1", "08:00", "08:40"},
            {"2", "08:50", "09:30"},
            {"3", "09:40", "10:20"},
            {"4", "10:50", "11:30"},
            {"5", "13:30", "14:10"},
            {"6", "14:20", "15:00"}
    };

    private static final String[][] JUNIOR_PERIODS = {
            {"1", "08:00", "08:45"},
            {"2", "08:55", "09:40"},
            {"3", "10:00", "10:45"},
            {"4", "10:55", "11:40"},
            {"5", "13:30", "14:15"},
            {"6", "14:25", "15:10"},
            {"7", "15:20", "16:05"}
    };

    /** 年级 sort_order -> (科目编码 -> 周节数) */
    private static final Map<Integer, Map<String, Integer>> PLANS = new HashMap<>();

    static {
        PLANS.put(1, plan("CHINESE", 8, "MATH", 5, "PE", 4, "ART", 2, "SCIENCE", 1,
                "MUSIC", 2, "CLASS_MEETING", 1, "SELF_STUDY", 7));
        PLANS.put(2, plan("CHINESE", 8, "MATH", 5, "PE", 4, "ART", 2, "SCIENCE", 2,
                "MORAL", 2, "MUSIC", 2, "CLASS_MEETING", 1, "SELF_STUDY", 4));
        PLANS.put(3, plan("CHINESE", 7, "MATH", 5, "ENGLISH", 2, "PE", 4, "ART", 2,
                "SCIENCE", 2, "MORAL", 2, "MUSIC", 2, "CLASS_MEETING", 1, "SELF_STUDY", 3));
        PLANS.put(4, plan("CHINESE", 7, "MATH", 5, "ENGLISH", 3, "PE", 4, "ART", 2,
                "SCIENCE", 2, "MORAL", 2, "MUSIC", 2, "CLASS_MEETING", 1, "SELF_STUDY", 2));
        PLANS.put(5, plan("CHINESE", 7, "MATH", 5, "ENGLISH", 3, "PE", 4, "ART", 2,
                "SCIENCE", 2, "MORAL", 2, "MUSIC", 2, "CLASS_MEETING", 1, "SELF_STUDY", 2));
        PLANS.put(6, plan("CHINESE", 7, "MATH", 5, "ENGLISH", 3, "PE", 4, "ART", 2,
                "SCIENCE", 2, "MORAL", 2, "MUSIC", 2, "CLASS_MEETING", 1, "SELF_STUDY", 2));
        PLANS.put(7, plan("CHINESE", 6, "MATH", 6, "ENGLISH", 6, "PE", 3, "POLITICS", 2,
                "BIOLOGY", 2, "PHYSICS", 2, "CHEMISTRY", 2, "ART", 1,
                "CLASS_MEETING", 1, "SELF_STUDY", 4));
        PLANS.put(8, plan("CHINESE", 6, "MATH", 6, "ENGLISH", 6, "PE", 3, "POLITICS", 2,
                "BIOLOGY", 2, "PHYSICS", 3, "CHEMISTRY", 2, "ART", 1,
                "CLASS_MEETING", 1, "SELF_STUDY", 3));
        PLANS.put(9, plan("CHINESE", 6, "MATH", 6, "ENGLISH", 6, "PE", 3, "POLITICS", 2,
                "BIOLOGY", 2, "PHYSICS", 3, "CHEMISTRY", 3, "ART", 1,
                "CLASS_MEETING", 1, "SELF_STUDY", 2));
    }

    private final SubjectRepository subjectRepository;
    private final PeriodRepository periodRepository;
    private final GradeSubjectRepository gradeSubjectRepository;
    private final GradeRepository gradeRepository;

    public TimetableDataInitializer(SubjectRepository subjectRepository,
                                    PeriodRepository periodRepository,
                                    GradeSubjectRepository gradeSubjectRepository,
                                    GradeRepository gradeRepository) {
        this.subjectRepository = subjectRepository;
        this.periodRepository = periodRepository;
        this.gradeSubjectRepository = gradeSubjectRepository;
        this.gradeRepository = gradeRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedSubjects();
        seedPeriods();
        seedPlans();
    }

    private void seedSubjects() {
        for (String[] row : SUBJECTS) {
            if (subjectRepository.existsByCode(row[1])) {
                continue;
            }
            Subject subject = new Subject();
            subject.setName(row[0]);
            subject.setCode(row[1]);
            subject.setCategory(row[2]);
            subject.setEnabled(1);
            subject.setSortOrder(Integer.parseInt(row[3]));
            subjectRepository.save(subject);
        }
    }

    private void seedPeriods() {
        if (periodRepository.countByStage("PRIMARY") == 0) {
            insertPeriods("PRIMARY", PRIMARY_PERIODS);
        }
        if (periodRepository.countByStage("JUNIOR") == 0) {
            insertPeriods("JUNIOR", JUNIOR_PERIODS);
        }
    }

    private void insertPeriods(String stage, String[][] rows) {
        for (String[] row : rows) {
            Period period = new Period();
            period.setStage(stage);
            period.setPeriodNo(Integer.parseInt(row[0]));
            period.setStartTime(LocalTime.parse(row[1]));
            period.setEndTime(LocalTime.parse(row[2]));
            periodRepository.save(period);
        }
    }

    private void seedPlans() {
        List<Grade> grades = gradeRepository.findAllByOrderBySortOrderAsc();
        for (Grade grade : grades) {
            Map<String, Integer> plan = PLANS.get(grade.getSortOrder());
            if (plan == null || !gradeSubjectRepository.findByGradeId(grade.getId()).isEmpty()) {
                continue;
            }
            for (Map.Entry<String, Integer> item : plan.entrySet()) {
                subjectRepository.findByCode(item.getKey()).ifPresent(subject -> {
                    GradeSubject row = new GradeSubject();
                    row.setGradeId(grade.getId());
                    row.setSubjectId(subject.getId());
                    row.setWeeklyPeriods(item.getValue());
                    gradeSubjectRepository.save(row);
                });
            }
        }
    }

    private static Map<String, Integer> plan(Object... pairs) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((String) pairs[i], (Integer) pairs[i + 1]);
        }
        return map;
    }
}
