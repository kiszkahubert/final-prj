import { Routes } from '@angular/router';
import { Home } from '../features/home/home';
import { Register } from '../features/account/register/register';
import { Login } from '../features/account/login/login';
import { authGuard } from '../core/guards/auth-guard';
import { guestGuard } from '../core/guards/guest-guard';
import { NotFound } from '../shared/errors/not-found/not-found';
import { ServerError } from '../shared/errors/server-error/server-error';
import { KidsManagementPanel } from '../features/kids/kids-management-panel/kids-management-panel';
import { KidForm } from '../features/kids/kid-form/kid-form';
import { SuggestionPanel } from '../features/suggestions/suggestion-panel/suggestion-panel';
import { Profile } from '../features/profile/profile';
import { Chat } from '../features/chat/chat';
import { Calendar } from '../features/tasks/calendar/calendar';
import { Gallery } from '../features/media/gallery/gallery';

export const routes: Routes = [
    { path: '', redirectTo: 'login', pathMatch: 'full' },
    { path: 'login', component: Login, canActivate: [guestGuard] },
    { path: 'register', component: Register, canActivate: [guestGuard] },
    {
        path: '',
        runGuardsAndResolvers: 'always',
        canActivate: [authGuard],
        children: [
            { path: 'home', component: Home },
            { path: 'profile', component: Profile },
            {
                path: 'kids',
                children: [
                    { path: '', component: KidsManagementPanel },
                    { path: 'new', component: KidForm },
                    { path: 'edit/:id', component: KidForm }
                ]
            },
            { path: 'suggestions', component: SuggestionPanel },
            { path: 'chat', component: Chat },
            { path: 'calendar', component: Calendar },
            { path: 'gallery', component: Gallery }
        ]
    },
    { path: 'server-error', component: ServerError },
    { path: '**', component: NotFound }
];
