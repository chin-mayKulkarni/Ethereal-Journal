import express from "express";
import path from "path";
import { createServer as createViteServer } from "vite";
import { GoogleGenAI, Type } from "@google/genai";
import dotenv from "dotenv";

dotenv.config();

const app = express();
const PORT = 3000;

app.use(express.json());

// Lazy-initialized Gemini Client
let aiInstance: GoogleGenAI | null = null;

function getGemini(): GoogleGenAI | null {
  const key = process.env.GEMINI_API_KEY;
  if (!key || key === "MY_GEMINI_API_KEY") {
    console.warn("Valid GEMINI_API_KEY is not set. Summaries will fall back to smart mock generator.");
    return null;
  }
  if (!aiInstance) {
    aiInstance = new GoogleGenAI({
      apiKey: key,
      httpOptions: {
        headers: {
          'User-Agent': 'aistudio-build',
        },
      },
    });
  }
  return aiInstance;
}

// REST API for AI Summary Generation
app.post("/api/summarize", async (req, res) => {
  try {
    const { entries, date, refinement } = req.body;

    if (!entries || !Array.isArray(entries) || entries.length === 0) {
      return res.json({
        milestones: [
          "Wrote no journal entries for today yet.",
          "First reflection is the first step to daily mindfulness.",
        ],
        actionableVectors: [
          "Dedicate 5 minutes to write about your morning thoughts, feelings, or tasks.",
          "Use voice mode to dictate a quick reflection of your current state.",
        ],
        emotionalTone: [
          "Quiet anticipation of the journaling journey ahead.",
          "Neutral and receptive mood.",
        ],
      });
    }

    const ai = getGemini();

    if (!ai) {
      // Return high-quality mock response matching written entries
      const mockSummary = generateSmartMockSummary(entries, date);
      return res.json(mockSummary);
    }

    const prompt = `You are a Sophisticated Confidant, an AI companion for a high-end personal journal application called "Ethereal Journal".
Analyze the user's journal entries for ${date} and generate an insightful, highly polished daily summary.

Format your analysis exactly as specified in the JSON response schema. Ensure the tone is elegant, intellectual, supportive, and sophisticated.

${refinement ? `REFINEMENT INSTRUCTION: The user has asked to refine the previous summary with: "${refinement}". Please adjust your tone or focus points according to this request.` : ""}

Journal Entries for ${date}:
${entries.map((e: any, index: number) => `Entry ${index + 1} (${e.time || 'Unknown Time'} - Mood: ${e.mood || 'Neutral'}):\n"${e.text}"`).join("\n\n")}`;

    const response = await ai.models.generateContent({
      model: "gemini-3.5-flash",
      contents: prompt,
      config: {
        systemInstruction: "You are an elegant, mindful therapist and executive coach. Focus on high-value bullet points.",
        responseMimeType: "application/json",
        responseSchema: {
          type: Type.OBJECT,
          properties: {
            milestones: {
              type: Type.ARRAY,
              items: { type: Type.STRING },
              description: "A list of 2-4 achievements, flow state moments, breakthroughs, or key reflective milestones achieved today.",
            },
            actionableVectors: {
              type: Type.ARRAY,
              items: { type: Type.STRING },
              description: "A list of 2-4 actionable suggestions, vectors, mindful steps, or executive-coaching-style questions to guide tomorrow.",
            },
            emotionalTone: {
              type: Type.ARRAY,
              items: { type: Type.STRING },
              description: "A list of 2-3 deep, highly articulate insights into the user's emotional state, energy levels, or underlying psychological dynamics.",
            },
          },
          required: ["milestones", "actionableVectors", "emotionalTone"],
        },
      },
    });

    const text = response.text;
    if (!text) {
      throw new Error("No response text from Gemini API");
    }

    const parsed = JSON.parse(text);
    return res.json(parsed);

  } catch (error: any) {
    console.error("Gemini Generation Error:", error);
    // Fallback to beautiful smart mock generator on failure
    const fallback = generateSmartMockSummary(req.body.entries || [], req.body.date || "Today");
    return res.json(fallback);
  }
});

// Helper function to generate sophisticated summaries offline/as fallback
function generateSmartMockSummary(entries: any[], date: string) {
  const lowerTexts = entries.map(e => e.text.toLowerCase()).join(" ");
  
  const milestones = [
    "Cultivated self-awareness by logging reflections on " + date + ".",
  ];
  const actionableVectors = [
    "Dedicate 10 minutes tomorrow morning to review today's takeaways.",
  ];
  const emotionalTone = [
    "Reflective and mindful of personal developments.",
  ];

  // Tailor based on keywords in the entries
  if (lowerTexts.includes("focused") || lowerTexts.includes("work") || lowerTexts.includes("project") || lowerTexts.includes("design")) {
    milestones.push("Achieved a productive flow state centered on professional drafting and architecture.");
    actionableVectors.push("Protect your focus block tomorrow by silencing notifications for the first 3 hours.");
    emotionalTone.push("High level of professional efficacy, combined with a structured executive drive.");
  } else {
    milestones.push("Sustained focus and intentional presence throughout the day's events.");
    actionableVectors.push("Consider reflecting on any micro-moments of joy or alignment you felt.");
    emotionalTone.push("Slight calm and centered stability, reflecting a balanced energy level.");
  }

  if (lowerTexts.includes("stress") || lowerTexts.includes("tired") || lowerTexts.includes("anxious") || lowerTexts.includes("worry")) {
    milestones.push("Confronted stressful variables directly through written documentation.");
    actionableVectors.push("Implement a 4-7-8 breathing exercise tonight to ease somatic tension.");
    emotionalTone.push("Contains subtle threads of adaptive anxiety, mitigated by structured outlet writing.");
  } else if (lowerTexts.includes("happy") || lowerTexts.includes("grateful") || lowerTexts.includes("excited") || lowerTexts.includes("love")) {
    milestones.push("Anchored deep gratitude for social connection and positive micro-experiences.");
    actionableVectors.push("Verbalize this appreciation to someone in your circle tomorrow.");
    emotionalTone.push("Dominant feeling of relational safety, appreciation, and lighthearted buoyancy.");
  } else {
    milestones.push("Navigated interpersonal elements with professional grace and calm communication.");
    actionableVectors.push("Dedicate some time tomorrow evening to structured resting or creative hobbies.");
    emotionalTone.push("Dominant feeling of professional efficacy and structured calm.");
  }

  return { milestones, actionableVectors, emotionalTone };
}

// Start Vite dev server or serve production dist
async function setupServer() {
  if (process.env.NODE_ENV !== "production") {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: "spa",
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(process.cwd(), "dist");
    app.use(express.static(distPath));
    app.get("*", (req, res) => {
      res.sendFile(path.join(distPath, "index.html"));
    });
  }

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`Server running at http://localhost:${PORT}`);
  });
}

setupServer();
