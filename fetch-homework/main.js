"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const thu_learn_lib_1 = require("thu-learn-lib");
const types_1 = require("thu-learn-lib/lib/types");
let helper;
let semester;
let courses;
async function core(username, password, semesterId) {
    if (!helper)
        helper = new thu_learn_lib_1.Learn2018Helper({ provider: () => { return { username, password }; } });
    if (!semesterId)
        semesterId = (await helper.getCurrentSemester()).id;
    if (!courses)
        courses = await helper.getCourseList(semesterId);
    const homeworks = await helper.getAllContents(courses.map(value => value.id), types_1.ContentType.HOMEWORK);
    const courseNames = courses.map(value => {
        return {
            name: value.name,
            id: value.id
        };
    });
    return { courseNames, homeworks };
}
function homeworkSuccessCb(data) {
    java.homeworkData(JSON.stringify(data));
}
function getHomework(username, password, semesterId) {
    core(username, password, semesterId).then(data => homeworkSuccessCb(data));
}
exports.getHomework = getHomework;
//@ts-ignore
window.getHomework = getHomework;
//# sourceMappingURL=main.js.map