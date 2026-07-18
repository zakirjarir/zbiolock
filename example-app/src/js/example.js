import { ZBioLock } from 'zbiolock';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    ZBioLock.echo({ value: inputValue })
}
