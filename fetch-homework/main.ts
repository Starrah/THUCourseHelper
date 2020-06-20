import { Learn2018Helper } from 'thu-learn-lib';
import { ContentType, SemesterInfo, CourseInfo } from 'thu-learn-lib/lib/types'

let helper: Learn2018Helper;
let semester: SemesterInfo;
let courses: CourseInfo[];

declare var java: any;

async function core(username, password, semesterId) {
    if (!helper) helper = new Learn2018Helper({provider: () => {return {username , password};}});
    if (!semesterId) semesterId = (await helper.getCurrentSemester()).id;
    if (!courses) courses = await helper.getCourseList(semesterId);
    const homeworks = await helper.getAllContents(courses.map(value => value.id), ContentType.HOMEWORK);
    const courseNames = courses.map(value => { return {
        name: value.name,
        id: value.id
    } });
    return {courseNames, homeworks}
}

function homeworkSuccessCb(data) {
    java.homeworkData(JSON.stringify(data));
}

function getHomework(username, password, semesterId) {
    core(username, password, semesterId).then(data => homeworkSuccessCb(data))
}

//@ts-ignore
window.getHomework = getHomework;

export {getHomework}

