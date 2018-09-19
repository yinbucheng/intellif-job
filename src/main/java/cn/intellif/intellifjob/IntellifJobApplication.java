package cn.intellif.intellifjob;

import cn.intellif.intellifjob.job.MyIntellifJob;
import cn.intellif.intellifjob.utils.IntellifSimpleJobUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IntellifJobApplication {

	public static void main(String[] args) {
		SpringApplication.run(IntellifJobApplication.class, args);

		IntellifSimpleJobUtils.initSimpleJobs(MyIntellifJob.class);
		IntellifSimpleJobUtils.startJob();
	}
}
