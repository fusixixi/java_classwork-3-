package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher,Integer> {
    Optional<Teacher> findByPersonNum(String num);
    @Query(value = "from Teacher where ?1='' or person.num like concat('%', ?1, '%') or person.name like concat('%', ?1, '%') ")
    List<Teacher> findTeacherListByNumName(String numName);
}
