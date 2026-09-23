# ANDRACORP
![System Diagram](https://github.com/Andradem11/AndraCorp/blob/main/AndraCorp%20Diagram.png?raw=true)
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
	
#Week 2

I started setting up the backend structure based on my system diagram. I created the main APIs and method prototypes for authentication, project management, workforce management, materials and equipment, expenses and budgets, change orders, reports and notifications, the compute engine, and data storage. I also initiated the Compute Engine structure with the ComputeEngineAPI, ComputationComponent, and JobManager. Some of the classes are not implemented yet and will be added later on.	

#Week 3
I created the main model and data classes for AndraCorp based on the system diagram and the APIs created in Week 2. These classes represent the main data that will be used throughout the system.

The model classes include:
- Users
- Projects
- Workers
- Work hours
- Materials
- Equipment
- Expenses
- Change orders
- Schedules and tasks
- Daily reports
- Photos
- Payments
- Audit logs

I also created the data classes needed for the Compute Engine. These classes will be used to represent project data, schedule and cost impacts, worker reallocation requests/results, delay forecasts, and computation jobs.

The model and data classes created this week will be used for the prototype implementations in Week 4.

#Week 4
I started implementing the main prototype components for AndraCorp using the APIs and model classes created in Weeks 2 and 3.

The prototype implementations include:

	-Project Management: Create, update, retrieve, and delete projects.
	-User Management: Create and manage contractor and client accounts.
	-Workforce Management: Add workers, assign and reassign them to project tasks, and record work hours.
	-Expenses and Budgets: Record project expenses and calculate remaining budgets.
	-Change Orders: Create change requests and allow clients to approve or decline them.

I created a new applicationimpl package to separate the implementations from the API interfaces. For now, I used HashMaps and ArrayLists to temporarily store information until I implement the database.

I also added JUnit 5 smoke tests to test the basic functionality of each component and updated the Gradle configuration to include the JUnit Platform launcher.

The next step is to implement the storage component and connect it to the prototype implementations.

