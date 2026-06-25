import React, { useState, useEffect } from "react";
import PhoneFrame from "./components/PhoneFrame";
import OnboardingScreen from "./components/OnboardingScreen";
import DashboardScreen from "./components/DashboardScreen";
import { DiaryEntry, AISummary, AppScreen } from "./types";

const LOCAL_STORAGE_ENTRIES_KEY = "ethereal_diary_entries_v1";
const LOCAL_STORAGE_USER_KEY = "ethereal_user_session_v1";

const DEFAULT_ENTRY_TEXT = 
  "Woke up feeling exceptionally focused today. The air was crisp, and the silence of the early morning provided the perfect canvas for drafting the new project proposal...";

const DEFAULT_ENTRIES: DiaryEntry[] = [
  {
    id: "default-entry-1",
    text: DEFAULT_ENTRY_TEXT,
    date: "2026-06-25",
    time: "08:30 AM",
    mood: "Focused",
  }
];

export default function App() {
  const [user, setUser] = useState<{ name: string; email: string; avatar: string } | null>(null);
  const [entries, setEntries] = useState<DiaryEntry[]>([]);
  const [summary, setSummary] = useState<AISummary | null>(null);
  const [summaryLoading, setSummaryLoading] = useState(false);
  const [lastRefinement, setLastRefinement] = useState("");

  // Load user session & diary logs on startup
  useEffect(() => {
    const savedUser = localStorage.getItem(LOCAL_STORAGE_USER_KEY);
    if (savedUser) {
      setUser(JSON.parse(savedUser));
    }

    const savedEntries = localStorage.getItem(LOCAL_STORAGE_ENTRIES_KEY);
    if (savedEntries) {
      setEntries(JSON.parse(savedEntries));
    } else {
      setEntries(DEFAULT_ENTRIES);
    }
  }, []);

  // Save entries when modified
  const saveEntriesToStorage = (updated: DiaryEntry[]) => {
    setEntries(updated);
    localStorage.setItem(LOCAL_STORAGE_ENTRIES_KEY, JSON.stringify(updated));
  };

  const handleSignIn = (signedInUser: { name: string; email: string; avatar: string }) => {
    setUser(signedInUser);
    localStorage.setItem(LOCAL_STORAGE_USER_KEY, JSON.stringify(signedInUser));
  };

  const handleSignOut = () => {
    setUser(null);
    localStorage.removeItem(LOCAL_STORAGE_USER_KEY);
    setSummary(null);
  };

  const handleAddEntry = (text: string, mood: string) => {
    const now = new Date();
    let hours = now.getHours();
    const minutes = now.getMinutes().toString().padStart(2, "0");
    const ampm = hours >= 12 ? "PM" : "AM";
    hours = hours % 12;
    hours = hours ? hours : 12;
    
    const newEntry: DiaryEntry = {
      id: `entry-${Date.now()}`,
      text: text,
      date: "2026-06-25", // Hardcoded standard date matching our mockup timeline
      time: `${hours}:${minutes} ${ampm}`,
      mood: mood,
    };

    const updated = [newEntry, ...entries];
    saveEntriesToStorage(updated);
  };

  const handleDeleteEntry = (id: string) => {
    const filtered = entries.filter((e) => e.id !== id);
    saveEntriesToStorage(filtered);
  };

  // Triggers Gemini summary request from backend
  const fetchAISummary = async (currentEntries: DiaryEntry[], refinementText = "") => {
    setSummaryLoading(true);
    
    try {
      const response = await fetch("/api/summarize", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          entries: currentEntries,
          date: "June 25, 2026",
          refinement: refinementText,
        }),
      });

      if (!response.ok) {
        throw new Error("Failed to compile summary");
      }

      const data = await response.json();
      setSummary(data);
    } catch (err) {
      console.error("AI summarization failed:", err);
      // Fallback is handled server-side, but if server fails completely, set simple fallback
      setSummary({
        milestones: [
          "Cultivated self-awareness by logging reflections today.",
          "Explored personal thought processes with secure cloud sync.",
        ],
        actionableVectors: [
          "Dedicate tomorrow morning to administrative refinement to maintain momentum.",
          "Keep checking in with Ethereal Journal daily to discover emotional patterns.",
        ],
        emotionalTone: [
          "Dominant feeling of professional efficacy and structured calm.",
        ],
      });
    } finally {
      setSummaryLoading(false);
    }
  };

  const handleFetchSummary = (refinementText = "") => {
    setLastRefinement(refinementText);
    fetchAISummary(entries, refinementText);
  };

  return (
    <PhoneFrame>
      {!user ? (
        <OnboardingScreen onSignIn={handleSignIn} />
      ) : (
        <DashboardScreen
          user={user}
          onSignOut={handleSignOut}
          entries={entries}
          onAddEntry={handleAddEntry}
          onDeleteEntry={handleDeleteEntry}
          summary={summary}
          summaryLoading={summaryLoading}
          onFetchSummary={handleFetchSummary}
        />
      )}
    </PhoneFrame>
  );
}
