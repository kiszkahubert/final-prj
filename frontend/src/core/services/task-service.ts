import { Injectable, inject } from '@angular/core';
import { Task, CreateTask, UpdateTask } from '../../types/task';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment.development';

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private http = inject(HttpClient);
  private baseUrl = environment.apiUrl;

  getTodayTasks() {
    return this.http.get<Task[]>(this.baseUrl + 'api/tasks/today');
  }

  getTasks() {
    return this.http.get<Task[]>(this.baseUrl + 'api/tasks/parent');
  }

  addTask(task: CreateTask) {
    return this.http.post<Task>(this.baseUrl + 'api/tasks', task);
  }

  updateTask(id: number, payload: UpdateTask) {
    return this.http.put<Task>(this.baseUrl + 'api/tasks/' + id, payload);
  }

  deleteTask(id: number) {
    return this.http.delete<string>(this.baseUrl + 'api/tasks/' + id, { responseType: 'text' as 'json' });
  }
}
