package org.akaza.openclinica.controller.openrosa.processor;

import java.util.ArrayList;
import java.util.Date;

import org.akaza.openclinica.controller.openrosa.SubmissionContainer;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.dao.hibernate.SubjectDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.domain.datamap.Subject;
import org.akaza.openclinica.domain.user.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class StudySubjectProcessor implements Processor, Ordered {

    @Autowired
    StudySubjectDao studySubjectDao;
    @Autowired
    UserAccountDao userAccountDao;
    @Autowired
    SubjectDao subjectDao;
    
    @Override
    public void process(SubmissionContainer container) throws Exception {
        System.out.println("Executing study subject processor.");
        
        String studySubjectOid = container.getSubjectContext().get("studySubjectOID");
        if (studySubjectOid != null)  {
            StudySubject studySubject = studySubjectDao.findByOcOID(studySubjectOid);
            container.setSubject(studySubject);
        } else {
            UserAccount rootUser = userAccountDao.findByUserId(1);
            int nextLabel = studySubjectDao.findTheGreatestLabel() + 1;
            
            
            // create subject
            Subject subject = new Subject();
            subject.setGender('\0'); // setting null character
            subject.setUserAccount(rootUser);
            subject.setStatus(Status.AVAILABLE);
            Date currentDate = new Date();
            String uniqueIdentifier = "anonymous-" + String.valueOf(nextLabel) + "-" + Long.toString(currentDate.getTime());
            subject.setUniqueIdentifier(uniqueIdentifier);
            subjectDao.saveOrUpdate(subject);
            subject = subjectDao.findByUniqueIdentifier(uniqueIdentifier);

            // create study subject
            StudySubject studySubject = new StudySubject();
            //TODO: Why was the following line in OpenRosaServices?
            //subjectBean.setGender('\0'); // setting null character
            studySubject.setStudy(container.getStudy());
            studySubject.setSubject(subject);
            studySubject.setStatus(Status.AVAILABLE);
            studySubject.setUserAccount(rootUser);
            studySubject.setEnrollmentDate(new Date());
            studySubjectOid = studySubjectDao.getValidOid(studySubject,new ArrayList<String>());
            studySubject.setLabel(Integer.toString(nextLabel));
            studySubject.setOcOid(studySubjectOid);
            studySubjectDao.saveOrUpdate(studySubject);
            container.setSubject(studySubjectDao.findByOcOID(studySubjectOid));
      
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }

}
