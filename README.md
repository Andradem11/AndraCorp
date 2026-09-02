# ANDRACORP

Overview:

	-AndraCrop is a web-based construction project management and decision support system designed for contractors and clients.
	-This system will allow contractors to manage construction projects(workers, hours, supplies, daily reports, etc)
	-Clients are given a separate interface where they can monitor project progress, review expenses, work-force information, view/request change orders.
	-AdraCorp will also analyze how project changes affect cost, labor and scheduling.


Goal:

	-Allow contractors and clients to interact through different interfaces.
	-Track workers, trades, work hours, expenses, materials, and equipment.
	-Track project areas and tasks
	-Allow clients to review/request and approve or decline change orders.
	-Calculate how changes affect the project schedule and budget
	-Simulate worker reallocation between project tasks and forecast possible delays
	

The contractor can:

	- Create and manage projects
	- Add clients to projects
	- Add and manage workers
	- Assign workers to specific trades
	- Assign workers to project tasks or areas
	- Record work hours
	- Manage materials
	- Manage equipment
	- Record project expenses
	- Create and update schedules
	- Create change orders
	- Upload project photos
	- Submit daily reports
	- Review client requests
	- View project forecasts and risk information
	

The client can:

	- View their project
	- View project progress
	- View project schedule
	- View estimated completion dates
	- View project costs
	- View project photos
	- View daily progress reports
	- View the number of workers currently working on the project
	- View workers grouped by trade
	- View workers assigned to specific areas of the project
	- Review change orders
	- Approve or decline change orders
	- Request project changes
	- Request faster completion of a project task or area
	- Review the estimated cost and scheduling impact of requested changes
	

# Example:
Work Area:

	Kitchen
	Living Room
	Bathroom
	Master Bedroom
	Basement
	Exterior
	
Work Tasks:

	Plumbing
	Electrical
	Sheetrock
	Carpentry
	Flooring
	Painting
	
	*EACH TASK WILL CONTAIN:*
			Assigned workers
			Estimated labor hours
			Actual labor hours
			Start date
			Estimated completion date
			Current progress
			Dependencies
			Material requirements
			Labor cost
			
Management:

	| Worker   | Trade      | Current Assignment |
	|----------|------------|--------------------|
	| Worker 1 | Plumbing   | Kitchen            |
	| Worker 2 | Plumbing   | Kitchen            |
	| Worker 3 | Sheetrock  | Kitchen            |
	| Worker 4 | Sheetrock  | Living Room        |
	| Worker 5 | Electrical | Living Room        |
	+
	The Contractor will be able to assign and reassign workers to different tasks depending on their schedule.
	The client will be able to see summarized work-force information without being able to modify it directly.

Worker Reallocation:
	
	Current Schedule:
		Kitchen:
				- 6 workers
				- Estimated completion: 14 days

		Living Room:
				- 4 workers
				- Estimated completion: 7 days
				
The client may request that the kitchen be completed sooner

		AndraCorp will simulate 2 possible options:
		
			Option 1: Reallocate Existing Workers
				Move 2 workers from the Living Room to the Kitchen.
				Estimated result:
						- Kitchen completion: 3 days earlier
						- Living Room completion: delayed by 2 days
						- Additional labor cost: $0
						
			Option 2: Add Additional Workers
				Add 2 additional workers to the Kitchen.
				Estimated result:
						- Kitchen completion: 3 days earlier
						- Living Room completion: no delay
						- Additional labor cost: calculated by the system
	
	
	

