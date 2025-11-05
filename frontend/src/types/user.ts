export type User = {
    username: string;
    token: string;
    expiresIn: number;
    expiryAt?: number;
}

export type LoginResponse = {
    token: string;
    expiresIn: number;
}

export type AccountFormCreds = {
    username: string;
    password: string;
}