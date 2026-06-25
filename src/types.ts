export interface DiaryEntry {
  id: string;
  text: string;
  date: string; // YYYY-MM-DD
  time: string; // HH:MM AM/PM
  mood: string; // e.g. "Focused", "Reflective", "Anxious", "Grateful", "Peaceful"
  audioUrl?: string; // If narrated/recorded
}

export interface AISummary {
  milestones: string[];
  actionableVectors: string[];
  emotionalTone: string[];
}

export type AppScreen = "onboarding" | "dashboard";
