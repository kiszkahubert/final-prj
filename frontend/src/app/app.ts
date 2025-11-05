import { Component, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { Nav } from "../layout/nav/nav";

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Nav],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected router = inject(Router);

  showNav(): boolean {
    if (!this.router.navigated) return false;

    let route = this.router.routerState.snapshot.root as any;
    while (route.firstChild) {
      route = route.firstChild;
    }

    const path = route.routeConfig?.path || '';

    const hiddenPaths = ['login', 'register', 'server-error', '**'];
    return !hiddenPaths.includes(path);
  }
}
