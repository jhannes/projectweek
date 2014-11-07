var findOrCreatePerson = function(hours, person) {
	for (var i = 0; i < hours.length; i++) {
		if (hours[i].person === person) return hours[i];
	};

	var newPerson = { person: person, issues: [] };
	hours.push(newPerson);
	return newPerson;
}

var findOrCreateIssue = function(person, issue) {
	for (var i = 0; i < person.issues.length; i++) {
		if (person.issues[i].issue === issue) return person.issues[i];
	};

	var newIssue = { issue: issue };
	person.issues.push(newIssue);
	return newIssue;
}

var summarizeWorklogs = function(worklogs) {
	var hours = [];

	for (var i = 0; i < worklogs.length; i++) {
		var worklog = worklogs[i];
		var person = findOrCreatePerson(hours, worklog.person);
		var issue = findOrCreateIssue(person, worklog.issue);
		person.issues.sort(function(a, b) { return a.issue > b.issue ? +1 : -1; })
	};

	hours.sort(function(a, b) { return a.person > b.person ? +1 : -1; })

	return hours;
}
