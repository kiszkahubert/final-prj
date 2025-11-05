export type Task = {
	taskId: number;
	title: string;
	description: string;
	taskStart: string;
	taskEnd: string;
	status: TaskStatus;
	note: string;
	parentId: number;
	kidIds: number[];
	isSynced: any;
	kidNames: string[];
}

export type CreateTask = {
	title: string;
	description: string;
	taskStart: string;
	taskEnd: string;
	status: TaskStatus;
	note?: string;
	kidIds: number[];
	isSynced?: string | null;
};

export type UpdateTask = {
	title?: string;
	description?: string;
	taskStart?: string;
	taskEnd?: string;
	status?: TaskStatus;
	note?: string;
	kidIds?: number[];
	isSynced?: string | null;
};

export type TaskStatus = 'DONE' | 'PENDING' | 'MISSED';
