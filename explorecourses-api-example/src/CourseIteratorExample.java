import java.io.IOException;

import org.jdom2.JDOMException;

import edu.stanford.services.explorecourses.Course;
import edu.stanford.services.explorecourses.Department;
import edu.stanford.services.explorecourses.School;
import edu.stanford.services.explorecourses.Section;
import edu.stanford.services.explorecourses.ExploreCoursesConnection;
import java.io.File;
import java.io.FileWriter;

// import com.google.gson.Gson;
/** Prints a list of all courses offered at Stanford in the current academic year **/
public class CourseIteratorExample
{
	public static void main(String[] args) throws IOException, JDOMException
	{
		File catalog = new File("../catalog.txt");
		FileWriter writer = new FileWriter(catalog);
		ExploreCoursesConnection connection = new ExploreCoursesConnection();
		for(School s : connection.getSchools())
			for(Department d : s.getDepartments())
				for(Course c : connection.getCoursesByQuery(d.getCode()))
				{
					// String json = gson.toJson(c.getSections());
					// System.out.println(json);
					// writer.write("--seperator--\n");
					// writer.write(c.getSubjectCodePrefix() + c.getSubjectCodeSuffix() + "\n");
					// writer.write(c.getSubjectCodePrefix()+c.getSubjectCodeSuffix()+": "+c.getTitle() + "\n");
					System.out.println(c.getSubjectCodePrefix()+c.getSubjectCodeSuffix()+": "+c.getTitle());
					for (Section sec : c.getSections()) {
						//writer.write(sec.getSectionNumber() + ":" + sec.getTerm() + sec.getComponent() + "," )
						String data = IcsGenerator.generate_ics_for_section(sec, c);
						if (data == "") {
							continue;
						}
						try {
							String name = IcsGenerator.generate_file_name_for_section(sec, c);
							writer.write(name + "\n");
							File file = new File("../public/ics/" + name + ".ics");
							FileWriter fileWriter = new FileWriter(file);
							fileWriter.write(data);
							fileWriter.flush();
							fileWriter.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
		writer.flush();
		writer.close();
	}
}
