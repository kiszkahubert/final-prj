export type Kid = {
	id?: number;
	name: string;
	birthDate: string;
}

export type KidRequest = {
	name: string;
	birthDate: string;
}

export type ChildAccessToken = {
	pin: string;
	qrHash: string;
	kidId: number;
}