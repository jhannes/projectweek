
describe("summarize worklogs per day and task", function() {
	
	it("should find workers", function() {
		var worklogs = [
			{ person: 'Someone', issue: 'PW-001', hours: 4, date: new Date('2014/10/13') },
			{ person: 'Johannes', issue: 'PW-001', hours: 4, date: new Date('2014/10/13') },
			{ person: 'Johannes', issue: 'PW-001', hours: 4, date: new Date('2014/10/14') },
		];

		var hours = summarizeWorklogs(worklogs);

		expect(_.pluck(hours, 'person'))
			.to.eql(['Johannes', 'Someone'])
	});

	it("should find issues", function() {
		var worklogs = [
			{ person: 'Johannes', issue: 'PW-002', hours: 4, date: new Date('2014/10/14') },
			{ person: 'Johannes', issue: 'PW-001', hours: 4, date: new Date('2014/10/13') },
			{ person: 'Johannes', issue: 'PW-001', hours: 4, date: new Date('2014/10/13') },
		];

		var hours = summarizeWorklogs(worklogs);

		expect(_.pluck(hours[0].issues, 'issue'))
			.to.eql(['PW-001', 'PW-002']);
	});

	it("should list all week days", function() {

	});

})